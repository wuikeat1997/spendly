import type { Metadata, Viewport } from "next";
import "./globals.css";

export const metadata: Metadata = {
  applicationName: "Spendly",
  title: "Spendly | Safe-to-Spend Control System",
  description:
    "A pre-spend financial control system that tells you whether a purchase is safe before you pay.",
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: "Spendly",
  },
  formatDetection: {
    telephone: false,
  },
  manifest: "/manifest.webmanifest",
};

export const viewport: Viewport = {
  colorScheme: "light",
  themeColor: "#f3efe5",
  width: "device-width",
  initialScale: 1,
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
