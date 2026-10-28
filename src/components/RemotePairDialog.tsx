import { useEffect, useState } from "react";
import QRCode from "qrcode";
import { X } from "lucide-react";
import { remoteInfo, type RemoteInfo } from "@/lib/tv-launcher";

// Mostra o endereço e o QR para o celular abrir a tela do controle
// (a pagina e servida pela propria TV, nada precisa ser instalado no celular).
export function RemotePairDialog({ onClose }: { onClose: () => void }) {
  const [info, setInfo] = useState<RemoteInfo | null>(null);
  const [qr, setQr] = useState("");
  const [web, setWeb] = useState(false);

  useEffect(() => {
    let alive = true;
    const tick = () =>
      remoteInfo()
        .then((r) => {
          if (!alive) return;
          if (!r) setWeb(true);
          else setInfo(r);
        })
        .catch(() => {});
    tick();
    const t = setInterval(tick, 5000);
    return () => {
      alive = false;
      clearInterval(t);
    };
  }, []);

  useEffect(() => {
    if (!info?.url) return;
    QRCode.toDataURL(info.url, {
      margin: 1,
      width: 420,
      color: { dark: "#0a0c12", light: "#ffffff" },
    })
      .then(setQr)
      .catch(() => {});
  }, [info?.url]);

  return (
    <div className="fixed inset-0 z-40 grid place-items-center bg-background/85 p-6 backdrop-blur">
      <div className="w-full max-w-4xl rounded-2xl bg-tile p-8 md:p-10">
        <div className="flex items-start justify-between gap-6">
          <div>
            <h2 className="text-3xl font-bold">Controlar pelo celular</h2>
            <p className="mt-2 text-lg text-muted-foreground">O celular vira o controle — sem instalar nada.</p>
          </div>
          <button autoFocus onClick={onClose} aria-label="Fechar" className="tv-tile grid h-12 w-12 shrink-0 place-items-center rounded-xl">
            <X className="h-6 w-6" />
          </button>
        </div>

        {web ? (
          <p className="mt-8 text-xl text-muted-foreground">Abra o TV Home pelo APK na TV para ver o endereço do controle.</p>
        ) : !info?.ip ? (
          <p className="mt-8 text-xl text-muted-foreground">A TV ainda não pegou um endereço no Wi-Fi. Conecte a TV à rede e abra esta tela de novo.</p>
        ) : (
          <div className="mt-8 grid items-center gap-8 md:grid-cols-[minmax(0,300px)_minmax(0,1fr)]">
            <div className="grid place-items-center rounded-2xl bg-white p-4">
              {qr ? (
                <img src={qr} alt="Código para abrir o controle" className="h-56 w-56" />
              ) : (
                <div className="h-56 w-56 animate-pulse rounded bg-neutral-300" />
              )}
            </div>
            <div>
              <p className="text-sm uppercase tracking-[0.25em] text-muted-foreground">Endereço na TV</p>
              <p className="mt-2 break-all font-mono text-3xl md:text-4xl">{info.url}</p>
              <ol className="mt-6 list-decimal space-y-3 pl-5 text-lg text-muted-foreground">
                <li>Deixe o celular no mesmo Wi-Fi da TV.</li>
                <li>Aponte a câmera do celular para o código, ou digite o endereço no navegador.</li>
                <li>O controle abre no celular: setas, OK, volume, reprodução e atalhos.</li>
              </ol>
              <p className="mt-6 text-base text-muted-foreground">
                Quem estiver na sua rede Wi-Fi consegue controlar a TV. Você também pode usar o app Google TV, que tem
                mais botões.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
