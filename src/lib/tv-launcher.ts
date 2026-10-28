import { Capacitor, registerPlugin } from "@capacitor/core";

export type RemapStatus = { enabled: boolean; running: boolean; learning: boolean; keycode: number };

type TvLauncherPlugin = {
  open(o: { packages: string }): Promise<void>;
  openSettings(o: { action?: string | undefined }): Promise<void>;
  remapStatus(): Promise<RemapStatus>;
  startLearning(o: { packages: string }): Promise<void>;
  clearRemap(): Promise<void>;
};

const TvLauncher = registerPlugin<TvLauncherPlugin>("TvLauncher");

export const isNative = () => Capacitor.isNativePlatform();

export const YOUCINE_PACKAGES = ["com.youcine.app", "com.youcine", "com.youcine.tv", "br.com.youcine"];

export async function openApp(packages: string[]) {
  if (!isNative()) throw new Error("web");
  await TvLauncher.open({ packages: packages.join(",") });
}

export async function openSettings(action?: string) {
  if (!isNative()) throw new Error("web");
  await TvLauncher.openSettings({ action });
}

export async function remapStatus(): Promise<RemapStatus | null> {
  if (!isNative()) return null;
  return TvLauncher.remapStatus();
}

export const startLearning = () => TvLauncher.startLearning({ packages: YOUCINE_PACKAGES.join(",") });
export const clearRemap = () => TvLauncher.clearRemap();
