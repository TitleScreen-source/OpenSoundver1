# AudMora architecture notes

AudMora is the current product name. `OpenSound` remains in the package name and app id for now to avoid a noisy migration. Rename it deliberately later, in a separate session.

## Product direction

AudMora is a music app in the Spotify / SoundCloud space, but its differentiator is atmosphere:

- atmospheric artist profiles
- live visual mini-player
- track atmosphere editor
- cohesive dark visual style
- showcase / reels cases that set the target quality bar

The showcase code is a reference layer, not disposable prototype code. Keep it separate from regular app architecture.

Media and app size strategy lives in `docs/audmora-media-strategy.md`. The short version: keep AudMora as a lightweight app shell, and load music/atmosphere/user content by id, URL, and cache rather than bundling it into the APK.

## Current architecture slice

- `com.opensound.app.data`
  - Repository contracts for tracks and atmosphere scenes.
  - Feed contracts for Home, Search, Library, Artist Profile, and User Profile track lists.
  - Profile and user library contracts for account-facing display data.
  - Local catalog seed data and resource selection.
  - Local seed feed composition over the current catalog.
  - Local seed profile and library summaries for the current prototype.
  - In-memory atmosphere storage for the current prototype save flow.
  - This is the future place to hide API / database / cache sources behind repository APIs.
- `com.opensound.app.state`
  - App-level UI state and `AudMoraViewModel`.
  - User profile, featured artist profile, and user library summary as state data, not hardcoded screen text.
  - `PlaybackQueue` for the current playback context, next/previous track movement, shuffle, and repeat mode.
  - Playback state reducer for track selection, play/pause, progress, queue movement, shuffle/repeat, seek, and completion.
  - MainActivity should delegate state transitions here instead of owning feature state.
- `com.opensound.app.navigation`
  - Typed screen enum and bottom navigation.
  - Avoid route strings in UI code.
- `com.opensound.app.playback`
  - Playback interface and Compose side effects.
  - `AudioPlaybackEngine` is the app-facing contract.
  - `AudioPlaybackEngineFactory` maps `TrackAudioSource` to the current engine implementation.
  - `AndroidMediaPlayerAudioEngine` is the current Android implementation.
  - This is a stepping stone toward a richer playback service, MediaSession, or Media3 layer.
- `com.opensound.app.editor`
  - Track Studio editor state and section vocabulary.
  - Timeline/layer operations such as add, duplicate, delete, trim, snap, and protected-layer rules.
  - Character/text layer edit rules such as drag bounds, text cue length, and selected-layer fallback.
  - This keeps editor concepts typed before the large screen is split by feature area.
- `com.opensound.app.showcase`
  - Reference reels/profile visuals.
  - New visual showcase cases should live here or in a child package.

## Track identity

Tracks use a stable `TrackId`. This matters more than it first seems:

- titles are display text and can change
- different artists can publish tracks with the same title
- translations and edits should not break saved atmosphere scenes
- playback history, likes, editor drafts, comments, and uploads need a durable key

Do not key app state by `Track.title`. Use `Track.id`.

`Track.audioSource` is the current audio source metadata. Today it uses `TrackAudioSource.LocalRawResource` for prototype files in `res/raw`, and it is accessed through `TrackRepository`. `Track.durationSeconds` is prototype duration metadata in the catalog, used by playback UI and seek clamping. Later the source and duration can come from API metadata, Media3, a media id, or a cached file reference without making screens know where the audio comes from.

Atmosphere scenes are accessed through `AtmosphereRepository`. The current `InMemoryAtmosphereRepository` keeps the prototype simple, but the app-facing contract is already shaped like durable per-track storage: read all known configs, read one config by `TrackId`, and save one config by `TrackId`. This prevents screens and ViewModels from becoming accidental storage layers.

`Track.visualMode` describes how the track should be rendered visually. A showcase/reels track is a visual mode, not a separate kind of audio logic. This keeps the reference content useful without making the product architecture depend on one demo case.

## Playback boundary

UI and app state should not call Android `MediaPlayer` directly. They should describe intent: selected audio source, desired play/pause state, progress updates, seek requests, and completion.

`AudioPlaybackEffect` translates that state into playback commands. The current engine is still `MediaPlayer`, but it is hidden behind `AudioPlaybackEngine` and selected through `AudioPlaybackEngineFactory`. This matters because real music apps usually outgrow the basic player quickly:

- background playback
- lock-screen and notification controls
- Bluetooth/headset controls
- queue and next/previous track behavior
- buffering, streaming, and cache
- MediaSession / Media3 integration

Keeping the boundary small lets us replace the engine later without rewriting screens.

`AudMoraUiState.tracks` is the app/catalog list. Screen lists such as `homeTracks`, `searchTracks`, `libraryTracks`, `artistProfileTracks`, and `userProfileTracks` come from `TrackFeedRepository`. `PlaybackQueue.tracks` is the current playback context. Keep all three separate: catalog is the known universe, feeds are what screens show, and queue is what next/previous follows after playback starts.

`ProfileRepository` owns profile-facing data such as the current user's public identity, metrics, and the featured artist profile. `UserLibraryRepository` owns user library display data. Today both are seed repositories, but screens already consume `UserProfile`, `ArtistProfile`, and `UserLibrarySummary` from state. This matters because real profile and library data will eventually come from account storage, API responses, local cache, likes, playlists, uploads, and saved atmosphere scenes. Compose screens should not know which source produced that data.

`PlaybackQueue` owns the current queue source, track order, current index, shuffle flag, and repeat mode. UI code should ask for contextual track playback, previous/next movement, shuffle toggles, and repeat cycling through `AudMoraViewModel`, not calculate list indexes itself. Shuffle uses a deterministic prototype order today so behavior stays testable; later it can become a seeded/random playlist order inside the queue without changing screens. Repeat modes are `Off`, `All`, and `One`.

Playback UI state transitions live in `AudMoraPlaybackReducer`. This keeps app rules such as "switching tracks rewinds progress", "queue movement preserves play/pause", "seek requests are one-shot intents", "seek/progress clamps to track duration", "completion advances through the queue", and "repeat-one restarts the current track" testable before AudMora grows richer seeking, downloads, or background playback.

## Editor boundary

Track Studio is the future creative center of AudMora. It will likely grow into several domains:

- timeline clips and layer timing
- scene style and color presets
- character layer controls
- text cue controls
- source assets
- preview/playhead behavior

The current screen is still large, but editor state now uses `TrackStudioEditorState`, `TrackStudioSection`, and `TrackStudioEditorAction`. State transitions live in `TrackStudioEditorReducer`; Compose-observable editor state lives in `TrackStudioStateHolder`; timeline rules live in `TrackStudioTimelineOperations`; text/character edit rules live in `TrackStudioLayerOperations`; timeline UI lives in `TrackStudioTimelinePanel`; editor section UI lives in `TrackStudioEditorSections`; shared Track Studio controls live in `TrackStudioComponents`. This matters because section strings such as `Scene` or `Timing`, clip operations such as duplicate/delete/trim, layer rules such as text length or drag bounds, and timeline controls are separate reasons to change. Keeping product rules outside Compose makes them testable, and keeping large UI domains in their own files makes the editor easier to grow without losing the showcase-quality visual direction.

## Growth rules for future sessions

- Keep `MainActivity` thin: setup, theme, root composition only.
- Prefer typed state and events over loose strings and scattered `remember` state.
- Add new data through repositories first, even if the first implementation is local/mock.
- Let regular product UI and showcase/reference UI share models only when the model is truly stable.
- Do not split large visual files mechanically. Extract around real concepts: timeline, layers, cues, render effects, controls.
- Add tests for reducers, repositories, route mapping, and editor constraints before broad UI tests.
- Treat playback as state-driven: UI asks for play/pause/track changes, playback effects report progress/completion.

## Next likely architecture steps

- Rename user-facing code from OpenSound to AudMora where it does not force package/app-id churn.
- Split `TrackStudioScreen` by editor domains: timeline, scene style, character layer, text cue, assets.
- Replace `SeedTrackFeedRepository` with real Home/Search/Library/Profile feed implementations when the app gets API, Room, playlists, likes, or recommendations.
- Extend `UserLibraryRepository` from static summary data to saved track ids, liked tracks, playlists, and library actions.
- Decide when to migrate from `MediaPlayer` to Media3/ExoPlayer.
- Promote `TrackStudioStateHolder` to a ViewModel when Track Studio starts coordinating persistence, previews, and upload flows.
