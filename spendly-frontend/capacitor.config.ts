import type { CapacitorConfig } from "@capacitor/cli";

const serverUrl = process.env.CAPACITOR_SERVER_URL || "http://localhost:3000";

const config: CapacitorConfig = {
  appId: process.env.CAPACITOR_APP_ID || "com.spendly.app",
  appName: "Spendly",
  webDir: "native-shell",
  server: {
    url: serverUrl,
    cleartext: serverUrl.startsWith("http://"),
  },
  ios: {
    contentInset: "automatic",
    scrollEnabled: true,
  },
  android: {
    allowMixedContent: serverUrl.startsWith("http://"),
  },
};

export default config;
