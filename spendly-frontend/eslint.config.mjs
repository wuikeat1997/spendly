import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  // Override default ignores of eslint-config-next.
  globalIgnores([
    // Default ignores of eslint-config-next:
    ".next/**",
    "out/**",
    "build/**",
    "ios/App/App/public/**",
    "ios/App/build/**",
    "ios/App/CapApp-SPM/build/**",
    "ios/capacitor-cordova-ios-plugins/**",
    "next-env.d.ts",
    "codex-next-scaffold/**",
  ]),
]);

export default eslintConfig;
