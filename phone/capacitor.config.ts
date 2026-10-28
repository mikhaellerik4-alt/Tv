import type { CapacitorConfig } from "@capacitor/cli";

// App do celular: controla a TV pela rede do Wi-Fi.
// A pagina do controle e a mesma que a TV serve (native/remote.html),
// copiada para phone/www no momento da construcao.
const config: CapacitorConfig = {
  appId: "com.mikhaell.tvremote",
  appName: "Controle TV",
  webDir: "www",
  android: {
    allowMixedContent: true,
  },
  server: {
    // O servidor da TV e http:// (rede local), entao o app tambem usa http.
    androidScheme: "http",
    cleartext: true,
  },
};

export default config;
