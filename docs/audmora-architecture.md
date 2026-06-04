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
  - Atmosphere storage adapters: in-memory for seed/tests, SharedPreferences for current local editor saves.
  - Track Studio draft storage adapters: in-memory for seed/tests, SharedPreferences for current local autosave.
  - User library storage adapters: in-memory for seed/tests, SharedPreferences for current local persistence.
  - Local catalog seed data and resource selection.
  - Local seed feed composition over the current catalog.
  - Local seed profile and library summaries for the current prototype.
  - This is the future place to hide API / database / cache sources behind repository APIs.
- `com.opensound.app.state`
  - App-level UI state and `AudMoraViewModel`.
  - User profile, featured artist profile, and user library summary as state data, not hardcoded screen text.
  - Track Studio session state owned by `AudMoraViewModel` and exposed as `trackStudioEditorState`, including dirty draft, autosave restore, and close confirmation state.
  - `PlaybackQueue` for the current playback context, next/previous track movement, shuffle, and repeat mode.
  - Playback state reducer for track selection, play/pause, progress, queue movement, shuffle/repeat, seek, and completion.
  - MainActivity should delegate state transitions here instead of owning feature state.
- `com.opensound.app.navigation`
  - Typed screen enum and bottom navigation.
  - Avoid route strings in UI code.
- `com.opensound.app.playback`
  - Playback interface and Compose side effects.
  - `PlaybackMediaItem` as the app-facing description of the selected playable track: stable track id, title, artist, duration, and resolved audio source.
  - `AudioPlaybackEngine` is the app-facing contract.
  - `AudioPlaybackEngineFactory` maps a `PlaybackMediaItem` to the current engine implementation.
  - `AndroidMediaPlayerAudioEngine` is the current Android implementation.
  - This is a stepping stone toward a richer playback service, MediaSession, or Media3 layer.
- `com.opensound.app.editor`
  - Track Studio editor state and section vocabulary.
  - Timeline/layer operations such as add, duplicate, delete, trim, snap, and protected-layer rules.
  - Character/text layer edit rules such as drag bounds, text cue length, and selected-layer fallback.
  - This keeps editor concepts typed while the large screen is being split by feature area.
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

Atmosphere scenes are accessed through `AtmosphereRepository`. The app-facing contract is shaped like durable per-track storage: read all known configs, read one config by `TrackId`, and save one config by `TrackId`. Seed/tests use `InMemoryAtmosphereConfigStorage`; the Android app currently uses `SharedPreferencesAtmosphereConfigStorage` through `StoredAtmosphereRepository`. Default catalog atmospheres and user-saved overrides stay separate, so a saved editor result can replace one track's visual without mutating catalog seed data. This prevents screens and ViewModels from becoming accidental storage layers.

Track Studio drafts are accessed through `TrackStudioDraftRepository`, also keyed by `TrackId`. This is intentionally separate from `AtmosphereRepository`: a draft is unfinished local work, while an atmosphere save is the selected track's durable visual config. The Android app currently stores drafts in a separate SharedPreferences file through `LocalTrackStudioDraftRepositoryFactory`; tests use `InMemoryTrackStudioDraftRepository`. Dirty editor changes autosave into the draft repository, while reset, save, and discard clear the per-track draft. This keeps unfinished work recoverable without letting old drafts overwrite saved visual state or grow forever.

`Track.visualMode` describes how the track should be rendered visually. A showcase/reels track is a visual mode, not a separate kind of audio logic. This keeps the reference content useful without making the product architecture depend on one demo case.

## Playback boundary

UI and app state should not call Android `MediaPlayer` directly. They should describe intent: selected audio source, desired play/pause state, progress updates, seek requests, and completion.

`PlaybackMediaItem` is the playback layer's current media contract. It wraps the selected track's stable `TrackId`, display metadata, duration, and resolved `TrackAudioSource`. This matters because two tracks can temporarily share the same prototype audio file, but playback identity, history, notifications, queue state, cache keys, and future MediaSession metadata must still follow the track id.

`AudioPlaybackEffect` translates playback state and the selected `PlaybackMediaItem` into playback commands. The current engine is still `MediaPlayer`, but it is hidden behind `AudioPlaybackEngine` and selected through `AudioPlaybackEngineFactory`. This matters because real music apps usually outgrow the basic player quickly:

- background playback
- lock-screen and notification controls
- Bluetooth/headset controls
- queue and next/previous track behavior
- buffering, streaming, and cache
- MediaSession / Media3 integration

Keeping the boundary small lets us replace the engine later without rewriting screens.

`AudMoraUiState.tracks` is the app/catalog list. Screen lists such as `homeTracks`, `searchTracks`, `artistProfileTracks`, and `userProfileTracks` come from `TrackFeedRepository`. `libraryTracks` is derived from the current user's saved `TrackId` values in `UserLibraryRepository`, resolved against the known catalog. `PlaybackQueue.tracks` is the current playback context. Keep all four separate: catalog is the known universe, feeds are what discovery/profile screens show, library is what the user saved, and queue is what next/previous follows after playback starts.

`ProfileRepository` owns profile-facing data such as the current user's public identity, metrics, and the featured artist profile. `UserLibraryRepository` owns user library display data and saved track actions. Screens consume `UserProfile`, `ArtistProfile`, and `UserLibrarySnapshot` from state. Saved library track ids go through `UserLibraryStorage`: seed/tests use `InMemoryUserLibraryStorage`, and the Android app currently uses `SharedPreferencesUserLibraryStorage`. This is intentionally a small adapter, not a final storage commitment. It can later move to DataStore, Room, API sync, or a cache layer without making Compose screens know which source produced the data.

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

The current screen is being reduced into smaller UI domains. Editor state uses `TrackStudioEditorState`, `TrackStudioSection`, and `TrackStudioEditorAction`. State transitions live in `TrackStudioEditorReducer`; session state lives in `TrackStudioSessionStateHolder` and is owned by `AudMoraViewModel`; timeline rules live in `TrackStudioTimelineOperations`; text/character edit rules live in `TrackStudioLayerOperations`; timeline UI is coordinated by `TrackStudioTimelinePanel`, with reusable timeline chrome in `TrackStudioTimelineChrome`, row/trim UI in `TrackStudioTimelineLayerRow`, and labels/colors/time formatting in `TrackStudioTimelineFormatting`; editor section UI lives in `TrackStudioEditorSections`; selected-section routing lives in `TrackStudioSectionHost`; header, preview, tabs, save/reset bar, and close dialog live in `TrackStudioChrome`; shared Track Studio controls live in `TrackStudioComponents`. The session tracks both `draftConfig` and `savedConfig`, so `isDirty`, reset-to-saved, save, discard, autosave, per-track restore, and close confirmation stay out of the Compose screen. This matters because section strings such as `Scene` or `Timing`, clip operations such as duplicate/delete/trim, layer rules such as text length or drag bounds, and timeline controls are separate reasons to change. Keeping product rules outside Compose makes them testable, and keeping large UI domains in their own files makes the editor easier to grow without losing the showcase-quality visual direction.

## Growth rules for future sessions

- Keep `MainActivity` thin: setup, theme, root composition only.
- Prefer typed state and events over loose strings and scattered `remember` state.
- Add new data through repositories first, even if the first implementation is local/mock.
- Let regular product UI and showcase/reference UI share models only when the model is truly stable.
- Do not split large visual files mechanically. Extract around real concepts: timeline, layers, cues, render effects, controls.
- Do not over-design the unfinished editor. Prefer reversible UI extraction and tested state rules over final-looking abstractions.
- Add tests for reducers, repositories, route mapping, and editor constraints before broad UI tests.
- Treat playback as state-driven: UI asks for play/pause/track changes, playback effects report progress/completion.

## Next likely architecture steps

- Rename user-facing code from OpenSound to AudMora where it does not force package/app-id churn.
- Split `TrackStudioEditorSections` into separate domain files when scene, character, text, timing, or assets each start growing their own controls.
- Replace `SeedTrackFeedRepository` with real Home/Search/Library/Profile feed implementations when the app gets API, Room, playlists, likes, or recommendations.
- Extend `UserLibraryRepository` from saved track ids to liked tracks, playlists, folders, downloads, and sync conflict rules.
- Promote `SharedPreferencesUserLibraryStorage` to DataStore or Room when library state starts carrying richer metadata than a small ordered id list.
- Decide when Track Studio drafts need timestamps, explicit draft names, or cloud sync conflict rules.
- Decide when to migrate from `MediaPlayer` to Media3/ExoPlayer.
