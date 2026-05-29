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

## Current architecture slice

- `com.opensound.app.data`
  - Local catalog seed data and resource selection.
  - This is the future place to hide API / database sources behind repository APIs.
- `com.opensound.app.state`
  - App-level UI state and `AudMoraViewModel`.
  - MainActivity should delegate state transitions here instead of owning feature state.
- `com.opensound.app.navigation`
  - Typed screen enum and bottom navigation.
  - Avoid route strings in UI code.
- `com.opensound.app.playback`
  - Playback interface and Compose side effects.
  - `AudioPlaybackEngine` is the app-facing contract.
  - `AndroidMediaPlayerAudioEngine` is the current Android implementation.
  - This is a stepping stone toward a richer playback service, MediaSession, or Media3 layer.
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

`Track.audioResId` is the current local prototype audio source. Later this can become a URL, media id, or cached file reference behind a playback repository.

`Track.visualMode` describes how the track should be rendered visually. A showcase/reels track is a visual mode, not a separate kind of audio logic. This keeps the reference content useful without making the product architecture depend on one demo case.

## Playback boundary

UI and app state should not call Android `MediaPlayer` directly. They should describe intent: selected audio source, desired play/pause state, progress updates, and completion.

`AudioPlaybackEffect` translates that state into playback commands. The current engine is still `MediaPlayer`, but it is hidden behind `AudioPlaybackEngine`. This matters because real music apps usually outgrow the basic player quickly:

- background playback
- lock-screen and notification controls
- Bluetooth/headset controls
- queue and next/previous track behavior
- buffering, streaming, and cache
- MediaSession / Media3 integration

Keeping the boundary small lets us replace the engine later without rewriting screens.

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
- Add seek/progress semantics to the playback boundary.
- Decide when to migrate from `MediaPlayer` to Media3/ExoPlayer.
- Add a repository contract for tracks, profiles, and atmosphere scenes.
- Create a dedicated state holder for the editor draft instead of keeping all editor state inside `TrackStudioScreen`.
