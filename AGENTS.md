<!-- LOVABLE:BEGIN -->
> [!IMPORTANT]
> This project is connected to [Lovable](https://lovable.dev). Avoid rewriting
> published git history — force pushing, or rebasing/amending/squashing commits
> that are already pushed — as it rewrites history on Lovable's side and the
> user will likely lose their project history.
>
> Commits you push to the connected branch sync back to Lovable and show up in
> the editor, so keep the branch in a working state.
<!-- LOVABLE:END -->

- Android TV APK: Capacitor loads the published URL; native/ holds the launcher plugin, the remote-key remap service and the manifest patch — keeps the web app the single source of UI.
- The APK can be built two equivalent ways: the GitHub workflow (.github/workflows/android-tv-apk.yml) or locally (JDK 21 + Android SDK 35 + `cap add android`, copy native/ files into the package dir, run patch_manifest.py, `cap sync android`, `./gradlew assembleDebug` with APP_URL set to the published URL). Prefer local when the project has no GitHub connection, since git sync setup is a UI-only action the agent cannot do.
