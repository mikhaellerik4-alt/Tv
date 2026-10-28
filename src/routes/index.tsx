import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState } from "react";
import { Cast, Laptop, Play, Smartphone, Tv, Clapperboard, Settings, X } from "lucide-react";
import { openApp, openSettings, YOUCINE_PACKAGES } from "@/lib/tv-launcher";
import { RemapDialog } from "@/components/RemapDialog";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "TV Home — Tela inicial para Android TV" },
      { name: "description", content: "Tela inicial simples para Android TV com atalhos para YouCine, YouTube, Netflix, TV e espelhamento." },
      { property: "og:title", content: "TV Home — Tela inicial para Android TV" },
      { property: "og:description", content: "Atalhos grandes, relógio e espelhamento para sua TV." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary" },
    ],
    links: [
      { rel: "preconnect", href: "https://fonts.googleapis.com" },
      { rel: "stylesheet", href: "https://fonts.googleapis.com/css2?family=Sora:wght@300;500;700&display=swap" },
    ],
  }),
  component: Home,
});

type Tile = {
  name: string;
  icon: typeof Tv;
  hue: string;
  packages?: string[];
  settings?: string;
  info?: "mirror" | "dex" | "remote";
  homeSettings?: boolean;
};

const TILES: Tile[] = [
  { name: "YouCine", icon: Clapperboard, hue: "oklch(0.55 0.2 300)", packages: YOUCINE_PACKAGES },
  { name: "YouTube", icon: Play, hue: "oklch(0.58 0.23 27)", packages: ["com.google.android.youtube.tv", "com.google.android.youtube"] },
  { name: "Netflix", icon: Clapperboard, hue: "oklch(0.5 0.22 25)", packages: ["com.netflix.ninja", "com.netflix.mediaclient"] },
  { name: "TV", icon: Tv, hue: "oklch(0.55 0.13 230)", packages: ["com.google.android.tv", "com.android.tv", "com.google.android.apps.tv.launcherx"] },
  { name: "Espelhar tela", icon: Cast, hue: "oklch(0.6 0.15 160)", info: "mirror" },
  { name: "Tela sem fio (DeX)", icon: Laptop, hue: "oklch(0.6 0.14 200)", info: "dex" },
  { name: "Controle no celular", icon: Smartphone, hue: "oklch(0.7 0.15 75)", info: "remote" },
  { name: "Configurações", icon: Settings, hue: "oklch(0.45 0.02 250)", homeSettings: true },
];

type InfoEntry = { title: string; note?: string; steps: string[] };

const INFO: Record<"mirror" | "dex" | "remote", InfoEntry> = {
  mirror: {
    title: "Espelhar a tela do celular",
    steps: ["Conecte o celular no mesmo Wi‑Fi da TV.", "No celular, abra o painel rápido e toque em “Transmitir tela” / “Smart View”.", "Escolha o nome desta TV na lista."],
  },
  dex: {
    title: "Usar a TV como tela sem fio (DeX)",
    steps: ["No Samsung, abra o painel rápido e toque em “DeX”.", "Escolha esta TV (ela aparece como dispositivo de transmissão).", "Use o celular como touchpad ou conecte um teclado Bluetooth."],
  },
  remote: {
    title: "Controlar a TV pelo celular",
    note: "O controle remoto é outro app, que só existe para o celular — nada é instalado nesta TV.",
    steps: [
      "No celular, instale o app “Google TV” (Play Store ou App Store).",
      "Abra o app e toque no ícone de controle remoto.",
      "Escolha esta TV na lista e digite o código que aparecer na tela da TV.",
    ],
  },
};

function Home() {
  const [now, setNow] = useState<Date | null>(null);
  const [info, setInfo] = useState<keyof typeof INFO | null>(null);
  const [menu, setMenu] = useState(false);
  const [remap, setRemap] = useState(false);
  const [toast, setToast] = useState<string | null>(null);
  const refs = useRef<(HTMLButtonElement | null)[]>([]);
  const COLS = 4;

  useEffect(() => {
    setNow(new Date());
    const t = setInterval(() => setNow(new Date()), 10000);
    refs.current[0]?.focus();
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 3000);
    return () => clearTimeout(t);
  }, [toast]);

  const onKey = (e: React.KeyboardEvent, i: number) => {
    const map: Record<string, number> = { ArrowRight: 1, ArrowLeft: -1, ArrowDown: COLS, ArrowUp: -COLS };
    const d = map[e.key];
    if (d === undefined) return;
    e.preventDefault();
    const n = i + d;
    if (n >= 0 && n < TILES.length) refs.current[n]?.focus();
  };

  const launch = async (t: Tile) => {
    if (t.homeSettings) return setMenu(true);
    if (t.info) return setInfo(t.info);
    try {
      if (t.packages) await openApp(t.packages);
      else if (t.settings) await openSettings(t.settings);
    } catch (e) {
      setToast(e instanceof Error && e.message === "web" ? "Abra pelo APK na TV para iniciar apps." : `${t.name} não está instalado nesta TV.`);
    }
  };

  return (
    <main className="tv-sky min-h-screen px-[5vw] py-[6vh] font-sans text-foreground">
      <header className="flex items-end justify-between">
        <div>
          <p className="text-sm uppercase tracking-[0.3em] text-muted-foreground">Bem-vindo</p>
          <h1 className="text-4xl font-bold md:text-6xl">TV Home</h1>
        </div>
        <div className="text-right" suppressHydrationWarning>
          <p className="text-5xl font-light md:text-7xl">
            {now?.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" }) ?? "--:--"}
          </p>
          <p className="text-muted-foreground capitalize">
            {now?.toLocaleDateString("pt-BR", { weekday: "long", day: "numeric", month: "long" }) ?? ""}
          </p>
        </div>
      </header>

      <section className="mt-[8vh] grid grid-cols-2 gap-5 md:grid-cols-4 md:gap-8">
        {TILES.map((t, i) => (
          <button
            key={t.name}
            ref={(el) => {
              refs.current[i] = el;
            }}
            onKeyDown={(e) => onKey(e, i)}
            onClick={() => launch(t)}
            className="tv-tile flex aspect-video flex-col justify-between rounded-2xl p-5 text-left"
          >
            <span className="grid h-12 w-12 place-items-center rounded-xl" style={{ background: t.hue }}>
              <t.icon className="h-6 w-6" />
            </span>
            <span className="text-lg font-medium md:text-xl">{t.name}</span>
          </button>
        ))}
      </section>

      {toast && (
        <div className="fixed bottom-8 left-1/2 -translate-x-1/2 rounded-xl bg-tile px-6 py-3 text-lg">{toast}</div>
      )}

      {menu && (
        <div className="fixed inset-0 z-40 grid place-items-center bg-background/85 p-6 backdrop-blur">
          <div className="w-full max-w-xl rounded-2xl bg-tile p-8">
            <h2 className="text-2xl font-bold">Configurações</h2>
            <div className="mt-6 flex flex-col gap-3">
              <button
                autoFocus
                onClick={() => {
                  setMenu(false);
                  openSettings("android.settings.SETTINGS").catch(() => {});
                }}
                className="tv-tile rounded-xl px-5 py-4 text-lg text-left"
              >
                Ajustes da TV (Android)
              </button>
              <button
                onClick={() => {
                  setMenu(false);
                  setRemap(true);
                }}
                className="tv-tile rounded-xl px-5 py-4 text-lg text-left"
              >
                Atalho YouCine no controle
              </button>
              <button onClick={() => setMenu(false)} className="tv-tile rounded-xl px-5 py-4 text-lg text-left">
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}

      {remap && <RemapDialog onClose={() => setRemap(false)} />}

      {info && (
        <div className="fixed inset-0 grid place-items-center bg-background/80 p-6 backdrop-blur">
          <div className="w-full max-w-xl rounded-2xl bg-tile p-8">
            <h2 className="text-2xl font-bold">{INFO[info].title}</h2>
            {INFO[info].note && <p className="mt-2 text-base text-muted-foreground">{INFO[info].note}</p>}
            <ol className="mt-5 list-decimal space-y-3 pl-5 text-lg text-muted-foreground">
              {INFO[info].steps.map((s) => (
                <li key={s}>{s}</li>
              ))}
            </ol>
            <button autoFocus onClick={() => setInfo(null)} className="tv-tile mt-8 inline-flex items-center gap-2 rounded-xl px-6 py-3 text-lg">
              <X className="h-5 w-5" /> Fechar
            </button>
          </div>
        </div>
      )}
    </main>
  );
}
