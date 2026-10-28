import type { CapacitorConfig } from "@capacitor/cli";

// The APK loads the published web app. APP_URL is supplied by the GitHub workflow.
const url = process.env.APP_URL;

const config: CapacitorConfig = {
  appId: "com.mikhaell.tvlauncher",
  appName: "TV Home",
  webDir: "capacitor-www",
  server: url ? { url, cleartext: false } : undefined,
};

export default config;
