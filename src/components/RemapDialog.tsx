import { useEffect, useState } from "react";
import { AlertTriangle, CheckCircle2, X } from "lucide-react";
import { clearRemap, openSettings, remapStatus, startLearning, type RemapStatus } from "@/lib/tv-launcher";

export function RemapDialog({ onClose }: { onClose: () => void }) {
  const [st, setSt] = useState<RemapStatus | null>(null);
  const [web, setWeb] = useState(false);

  useEffect(() => {
    let alive = true;
    const tick = async () => {
      try {
        const s = await remapStatus();
        if (!alive) return;
        if (!s) setWeb(true);
        setSt(s);
      } catch {
        setWeb(true);
      }
    };
    tick();
    const t = setInterval(tick, 1500);
    return () => {
      alive = false;
      clearInterval(t);
    };
  }, []);

  const on = !!st?.enabled;

  return (
    <div className="fixed inset-0 z-50 grid place-items-center overflow-y-auto bg-background/85 p-6 backdrop-blur">
      <div className="w-full max-w-2xl rounded-2xl bg-tile p-8">
        <h2 className="text-2xl font-bold">Botão Globoplay → YouCine</h2>

        <ol className="mt-5 list-decimal space-y-3 pl-5 text-lg text-muted-foreground">
          <li>
            Ative a acessibilidade: <b className="text-foreground">Configurações → Sistema/Preferências do dispositivo → Acessibilidade → TV Home - Botão do controle → Ativar</b>.
          </li>
          <li>Volte aqui e toque em <b className="text-foreground">Gravar botão</b>.</li>
          <li>Aperte o botão <b className="text-foreground">Globoplay</b> no controle.</li>
          <li>Pronto: ao apertar esse botão, o YouCine deve abrir.</li>
        </ol>

        <div className="mt-6 rounded-xl bg-background/40 p-4 text-lg">
          {web ? (
            <p>Essa opção só funciona no APK instalado na TV.</p>
          ) : !on ? (
            <p className="flex items-center gap-2"><AlertTriangle className="h-5 w-5 text-tile-focus" /> Acessibilidade desativada.</p>
          ) : st?.learning ? (
            <p className="animate-pulse">Aguardando… aperte agora o botão Globoplay.</p>
          ) : st && st.keycode !== -1 ? (
            <p className="flex items-center gap-2"><CheckCircle2 className="h-5 w-5 text-tile-focus" /> Botão gravado (código {st.keycode}). Ele agora abre o YouCine.</p>
          ) : (
            <p>Acessibilidade ativa. Nenhum botão gravado ainda.</p>
          )}
        </div>

        <div className="mt-6 flex flex-wrap gap-3">
          <button autoFocus onClick={() => openSettings("android.settings.ACCESSIBILITY_SETTINGS").catch(() => {})} className="tv-tile rounded-xl px-5 py-3 text-lg">
            Abrir Acessibilidade
          </button>
          <button disabled={!on} onClick={() => startLearning()} className="tv-tile rounded-xl px-5 py-3 text-lg disabled:opacity-40">
            Gravar botão
          </button>
          <button disabled={!on} onClick={() => clearRemap()} className="tv-tile rounded-xl px-5 py-3 text-lg disabled:opacity-40">
            Remover
          </button>
          <button onClick={onClose} className="tv-tile inline-flex items-center gap-2 rounded-xl px-5 py-3 text-lg">
            <X className="h-5 w-5" /> Fechar
          </button>
        </div>

        <div className="mt-6 text-sm text-muted-foreground">
          <p className="font-semibold text-foreground">Limitações em TVs TCL</p>
          <ul className="mt-2 list-disc space-y-1 pl-5">
            <li>Em muitas TCL, o botão Globoplay é tratado pelo próprio sistema antes de chegar a qualquer app. Se a gravação ficar "Aguardando" para sempre, sua TV não permite trocar esse botão.</li>
            <li>Às vezes o Globoplay ainda abre por um instante antes do YouCine.</li>
            <li>Algumas TCL desativam a acessibilidade depois de reiniciar ou de uma atualização. Se parar de funcionar, ative de novo.</li>
            <li>Se o YouCine não abrir, o código do app pode ser diferente do esperado. Me envie o nome que aparece no app "Package Name Viewer".</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
