# EMOS TEST — Quote of the Day

An Android application that fetches and displays a daily inspirational quote from the [FavQs API](https://favqs.com/api), with the ability to save, browse, and manage favorite quotes locally.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Layer Breakdown](#layer-breakdown)
  - [Data Layer](#data-layer)
  - [Domain Layer](#domain-layer)
  - [UI Layer](#ui-layer)
- [Dependency Injection](#dependency-injection)
- [Navigation](#navigation)
- [State Management](#state-management)

---

## Tech Stack

| Category | Library / Tool |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | Clean Architecture + MVVM |
| Dependency Injection | Koin |
| Networking | Retrofit 2 |
| JSON Parsing | Moshi + KotlinJsonAdapterFactory |
| HTTP Client | OkHttp + Logging Interceptor |
| Local Database | Room |
| Async / Reactive | Kotlin Coroutines + Flow |
| Navigation | Compose Navigation (type-safe) |
| Image Loading | Coil (OkHttp engine) |
| Serialization | kotlinx.serialization |
| Annotation Processing | KSP |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 36 |

---

## Architecture

The app follows **Clean Architecture** with a strict three-layer separation:

```
┌─────────────────────────────────────┐
│              UI Layer               │  Composables · ViewModels · UiState
├─────────────────────────────────────┤
│            Domain Layer             │  Models · Error types
├─────────────────────────────────────┤
│             Data Layer              │  Repository · DAO · API Service · Mappers
└─────────────────────────────────────┘
```

**Data flows in one direction:**

```
API / Room  ──►  Repository  ──►  ViewModel  ──►  Composable UI
                                  (StateFlow)       (collectAsState)
```

**Key principles applied:**

- The Domain layer has zero Android or framework dependencies — plain Kotlin data classes and sealed errors only.
- The Data layer owns all I/O: remote (Retrofit) and local (Room). It exposes results to the ViewModel via `Result<T>` (one-shot) and `Flow<T>` (reactive streams).
- ViewModels hold and transform state but never reference Compose types.
- Composables are stateless beyond what they receive from a ViewModel via `StateFlow`.

---

## Project Structure

```
app/src/main/java/com/kadev/emostest/
│
├── App.kt                          # Application class — Koin initialisation
├── MainActivity.kt                 # Single activity — sets Compose content
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database definition (version 2)
│   │   ├── FavoriteQuoteDao.kt     # DAO: insert, delete, reactive query
│   │   └── FavoriteQuoteEntity.kt  # Room entity: fav_quote table
│   │
│   ├── mapper/
│   │   └── QuoteMapper.kt          # Extension fns: Response↔Domain↔Entity
│   │
│   ├── remote/
│   │   ├── ApiCall.kt              # safeApiCall() wrapper + HttpException mapper
│   │   └── ApiService.kt           # Retrofit interface: GET qotd
│   │
│   ├── repository/
│   │   └── QuoteRepository.kt      # Single source of truth for quote data
│   │
│   └── response/
│       └── QOTDResponse.kt         # Moshi-annotated API response model
│
├── di/
│   ├── AppModule.kt                # Repository + ViewModel bindings
│   ├── DatabaseModule.kt           # Room database + DAO bindings
│   └── NetworkModule.kt            # Moshi + OkHttp + Retrofit + ApiService
│
├── domain/
│   ├── error/
│   │   └── AppError.kt             # Sealed error hierarchy
│   └── model/
│       └── QuoteOfTheDay.kt        # Core domain model
│
└── ui/
    ├── AppNavigation.kt            # NavHost + route wiring
    ├── navigation/
    │   └── AppRoutes.kt            # @Serializable route objects
    ├── screen/
    │   ├── favorite/
    │   │   ├── FavoriteQuoteScreen.kt   # Composable UI for favorites
    │   │   └── FavoriteQuoteViewModel.kt
    │   └── home/
    │       ├── HomeScreen.kt            # Composable UI for QOTD
    │       └── HomeViewModel.kt
    └── theme/
        ├── Color.kt                # Indigo + Amber color tokens
        ├── Theme.kt                # MaterialTheme configuration
        └── Type.kt                 # Typography scale
```

---

## Layer Breakdown

### Data Layer

#### `FavoriteQuoteEntity` — Room Entity

```kotlin
@Entity(tableName = "fav_quote")
data class FavoriteQuoteEntity(
    @PrimaryKey val id: Long,
    val quote: String,
    val author: String,
    val timestamp: Long,       // System.currentTimeMillis() at insert time
)
```

The `id` is sourced from the FavQs API quote ID, so re-saving the same quote uses `OnConflictStrategy.REPLACE` rather than duplicating it.

---

#### `FavoriteQuoteDao` — Room DAO

```kotlin
@Dao
interface FavoriteQuoteDao {
    @Query("SELECT * FROM fav_quote")
    fun getFavQuote(): Flow<List<FavoriteQuoteEntity>>   // reactive — emits on every table change

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFav(fav: FavoriteQuoteEntity): Long?

    @Delete
    suspend fun deleteFav(fav: FavoriteQuoteEntity): Int?
}
```

`getFavQuote()` is intentionally **not** `suspend`. Room emits a new list automatically whenever rows are inserted or deleted, so the ViewModel never needs to manually re-query.

---

#### `AppDatabase` — Room Database

```kotlin
@Database(entities = [FavoriteQuoteEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteQuoteDao(): FavoriteQuoteDao
}
```

Built with `fallbackToDestructiveMigration(true)` — schema changes during development drop and recreate the database rather than requiring migration scripts.

---

#### `ApiService` — Retrofit Interface

```kotlin
interface ApiService {
    @GET("qotd")
    suspend fun getQuoteOfTheDay(): QOTDResponse
}
```

Single endpoint. Base URL is `https://favqs.com/api/`. The FavQs QOTD endpoint is public and requires no API key for basic access.

---

#### `QOTDResponse` — API Response Model

The raw JSON returned by the API has a nested structure:

```json
{
  "qotd_date": "2024-01-01",
  "quote": {
    "id": 12345,
    "body": "The quote text",
    "author": "Author Name",
    "author_permalink": "...",
    "dialogue": false,
    "private": false,
    "tags": [],
    "upvotes_count": 10,
    "downvotes_count": 1,
    "favorites_count": 5,
    "url": "..."
  }
}
```

Mapped to the domain model via `QuoteMapper.kt`.

---

#### `safeApiCall` — Error Handling Wrapper

```kotlin
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: HttpException) {
    Result.failure(e.toAppError())   // maps HTTP status codes to AppError subtypes
} catch (e: Exception) {
    Result.failure(AppError.from(e)) // maps timeouts, parse errors, etc.
}
```

All network calls go through this wrapper. It ensures the ViewModel always receives a `Result<T>` and never has to deal with raw exceptions.

---

#### `QuoteRepository` — Single Source of Truth

```kotlin
class QuoteRepository(val apiService: ApiService, val quoteDao: FavoriteQuoteDao) {

    suspend fun getQuoteOfTheDay(): Result<QuoteOfTheDay>          // one-shot remote fetch

    suspend fun insertQuoteToFavorite(quote: QuoteOfTheDay): Long? // write to DB

    fun getAllFavorite(): Flow<List<QuoteOfTheDay>>                 // reactive DB read

    suspend fun deleteFav(quote: QuoteOfTheDay): Int?              // delete from DB
}
```

`getAllFavorite()` returns a `Flow` — not a one-shot suspend function. This is the key design decision that makes the favorites list self-updating: when a row is deleted, Room emits the new list through the flow without any manual re-query.

---

#### `QuoteMapper` — Mapping Extension Functions

```kotlin
// API response → domain model
fun QOTDResponse.toDomain(): QuoteOfTheDay

// Room entity → domain model
fun FavoriteQuoteEntity.toDomain(): QuoteOfTheDay

// Domain model → Room entity (sets timestamp = now)
fun QuoteOfTheDay.toEntity(): FavoriteQuoteEntity
```

Mappers are top-level extension functions rather than a class, keeping them stateless and easy to test.

---

### Domain Layer

#### `QuoteOfTheDay` — Core Domain Model

```kotlin
data class QuoteOfTheDay(
    val id: Long,
    val quote: String,
    val author: String,
)
```

This is the only model the UI layer ever sees. The raw `QOTDResponse` and `FavoriteQuoteEntity` types never leak past the repository.

---

#### `AppError` — Sealed Error Hierarchy

```kotlin
sealed class AppError : Exception() {
    data class NetworkError(val statusCode: Int?, override val message: String) : AppError()
    data class DatabaseError(override val message: String) : AppError()
    data class ServerError(val statusCode: Int, override val message: String) : AppError()   // 5xx
    data class ClientError(val statusCode: Int, override val message: String) : AppError()   // 4xx
    data class TimeoutError(override val message: String = "Request timeout") : AppError()
    data class ParseError(override val message: String = "Failed to parse response") : AppError()
    data class UnknownError(override val message: String = "An unexpected error occurred") : AppError()

    companion object {
        fun from(throwable: Throwable): AppError  // maps any Throwable → AppError subtype
    }
}
```

HTTP errors are classified by status code range in `ApiCall.kt`:
- `400–499` → `ClientError`
- `500–599` → `ServerError`
- anything else → `NetworkError`

---

### UI Layer

#### `HomeViewModel`

Manages two independent state streams:

| State | Type | Description |
|---|---|---|
| `uiState` | `StateFlow<QOTDUiState>` | Drives the main quote card |
| `saveFavUiState` | `StateFlow<SaveFavUiState?>` | One-time event for snackbar feedback |

```
QOTDUiState:
  Loading  →  shown while fetching from network
  Success  →  quote card + Refresh/Favorite buttons
  Error    →  error message + Retry button

SaveFavUiState:
  null     →  idle / already consumed
  Success  →  "Added to favorites!" snackbar
  Error    →  "Failed to add to favorites." snackbar
```

**One-time event pattern:** `saveFavUiState` starts as `null`. After the save operation, it becomes `Success` or `Error`. The screen shows the snackbar then calls `viewModel.consumeSaveState()` which resets it to `null`. This prevents the snackbar from re-appearing when navigating back to the screen.

```
addQuoteToFavorite()
  └─► repository.insertQuoteToFavorite()
        ├─ returns non-null  →  saveFavUiState = Success
        └─ returns null      →  saveFavUiState = Error

consumeSaveState()
  └─► saveFavUiState = null
```

---

#### `FavoriteQuoteViewModel`

```kotlin
val uiState: StateFlow<FavQuoteUiState> = repository.getAllFavorite()
    .map { FavQuoteUiState.Success(it) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FavQuoteUiState.Loading
    )
```

The entire state management for favorites is a single pipeline:

1. `repository.getAllFavorite()` — a `Flow<List<QuoteOfTheDay>>` backed by Room
2. `.map { ... }` — wraps each emission in `FavQuoteUiState.Success`
3. `.stateIn(...)` — converts to `StateFlow` with `Loading` as the initial value; stops collecting 5 seconds after all UI subscribers leave to allow for configuration changes

When `deleteFav()` is called, it writes to Room, which automatically triggers a new emission from the DAO's flow, which propagates through the pipeline and updates the UI — **no manual reload needed**.

```
deleteFav(quote)
  └─► repository.deleteFav(quote)
        └─► Room table changes
              └─► DAO Flow emits new list
                    └─► uiState updates automatically
```

`SharingStarted.WhileSubscribed(5_000)` means the upstream Flow keeps running for 5 seconds after the last collector disappears (e.g., during a configuration change). This avoids re-querying the DB unnecessarily when the screen rotates.

---

#### `HomeScreen` — Composable

**State handling:**

```
uiState = Loading  →  CircularProgressIndicator (centered)
uiState = Success  →  ElevatedCard with quote text + author
                       Row with [Refresh] [Favorite] buttons
uiState = Error    →  Error message + [Retry] button
```

**Layout:**

- `Scaffold` with `CenterAlignedTopAppBar` (Indigo primary color)
- Vertical gradient background: `primary @ 10% alpha` → `surface`
- `Spacer(weight 1f)` / `Spacer(weight 1.5f)` to push the quote card toward the center and the "View Favorites" button toward the bottom
- `SnackbarHost` for save feedback

**Key effects:**

```kotlin
LaunchedEffect("QOTD") {
    viewModel.loadQOTD()  // fires once when the composable enters composition
}

LaunchedEffect(saveState) {
    // fires whenever saveState changes
    // shows snackbar then calls consumeSaveState() to reset to null
}
```

---

#### `FavoriteQuoteScreen` — Composable

**State handling:**

```
uiState = Loading        →  CircularProgressIndicator (centered)
uiState = Success(empty) →  EmptyState composable
uiState = Success(list)  →  LazyColumn of FavItem cards
uiState = Error          →  Error text (center)
```

**`FavItem` card layout:**
- `OutlinedCard` with `shapes.large` rounding
- Quote text in italic `bodyLarge`
- Author in `labelMedium` with secondary color
- 32×32dp `IconButton` with a `Delete` icon (error color, 70% alpha) aligned to the end

**`EmptyState` composable:**
- `FavoriteBorder` icon at 80dp, primary color at 30% alpha
- "No favorites yet" title
- "Quotes you heart will appear here" subtitle

No `LaunchedEffect` is needed here — the ViewModel's `StateFlow` is driven by Room's reactive query and starts collecting as soon as the composable collects it.

---

## Dependency Injection

Koin is initialised in `App.onCreate()` with three modules:

### `appModule`

```kotlin
single { QuoteRepository(get(), get()) }  // singleton repository

viewModel { HomeViewModel(get()) }
viewModel { FavoriteQuoteViewModel(get()) }
```

`get()` resolves `ApiService` and `FavoriteQuoteDao` from the other modules. ViewModels are scoped to their Compose host via `koinViewModel()`.

---

### `databaseModule`

```kotlin
single {
    Room.databaseBuilder(androidContext(), AppDatabase::class.java, "quote_database")
        .fallbackToDestructiveMigration(true)
        .build()
}

single { get<AppDatabase>().favoriteQuoteDao() }
```

Both the database and the DAO are singletons. The DAO is provided directly so the repository doesn't need to hold a reference to the whole database.

---

### `networkModule`

```kotlin
single { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }

single {
    OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = BODY })
        .connectTimeout(30, SECONDS)
        .readTimeout(30, SECONDS)
        .writeTimeout(30, SECONDS)
        .retryOnConnectionFailure(true)
        .build()
}

single {
    Retrofit.Builder()
        .baseUrl("https://favqs.com/api/")
        .client(get())                           // uses the OkHttpClient above
        .addConverterFactory(MoshiConverterFactory.create(get()))
        .build()
}

single { get<Retrofit>().create(ApiService::class.java) }
```

`KotlinJsonAdapterFactory` is required for Moshi to handle Kotlin data classes with default parameter values and `@param:Json` annotations.

---

## Navigation

Navigation uses **type-safe Compose Navigation** with `@Serializable` route objects instead of string routes.

### Routes

```kotlin
@Serializable object HomeRoute
@Serializable object FavoriteQuoteRoute
```

Route objects are serialized by `kotlinx.serialization` into the back stack. There are no arguments — data is loaded by each screen's ViewModel independently.

### Graph

```
HomeRoute  ──[navigate]──►  FavoriteQuoteRoute
               "View Favorites" button          │
                                                │ popBackStack()
                                                ◄── Back button / system back
```

The nav controller lives in `QuoteNavigation()` and is not leaked to screens. Screens receive only typed lambda callbacks (`onNavigateToFav: () -> Unit`, `onBack: () -> Unit`).

---

## State Management

### UiState Sealed Classes

```
QOTDUiState          (HomeViewModel)
├── Loading
├── Success(data: QuoteOfTheDay)
└── Error(error: AppError)

SaveFavUiState       (HomeViewModel — nullable, one-time event)
├── null             (idle / consumed)
├── Loading          (unused in current UI)
├── Success(data: Long)
└── Error(error: String)

FavQuoteUiState      (FavoriteQuoteViewModel)
├── Loading
├── Success(data: List<QuoteOfTheDay>)
└── Error(error: String)
```

### Data Flow Diagrams

**Home Screen — loading a quote:**

```
LaunchedEffect
    │
    ▼
viewModel.loadQOTD()
    │
    ▼
_uiState = Loading
    │
    ▼
repository.getQuoteOfTheDay()
    │    (safeApiCall wraps this)
    ├─ onSuccess ──► _uiState = Success(quote)
    └─ onFailure ──► _uiState = Error(appError)
```

**Home Screen — saving a favorite:**

```
"Favorite" button tapped
    │
    ▼
viewModel.addQuoteToFavorite(quote)
    │
    ▼
repository.insertQuoteToFavorite(quote)
    │    (calls quoteDao.insertFav)
    ├─ returns Long  ──► _saveFavUiState = Success
    └─ returns null  ──► _saveFavUiState = Error

LaunchedEffect(saveState) fires
    │
    ▼
snackbarHostState.showSnackbar(...)
    │
    ▼
viewModel.consumeSaveState()  →  _saveFavUiState = null
```

**Favorites Screen — reactive delete:**

```
Delete icon tapped
    │
    ▼
viewModel.deleteFav(quote)
    │
    ▼
repository.deleteFav(quote)
    │
    ▼
Room writes DELETE to fav_quote table
    │
    ▼  (automatic — Room observes table changes)
DAO Flow<List<FavoriteQuoteEntity>> emits new list
    │
    ▼
repository.getAllFavorite() map { toDomain() } emits
    │
    ▼
FavoriteQuoteViewModel.uiState emits Success(updatedList)
    │
    ▼
UI recomposes with item removed — no Loading state, no flicker
```