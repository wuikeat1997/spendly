import Link from "next/link";
import { MvpConsole } from "@/components/mvp-console";

export default function AppPage() {
  return (
    <main className="grain min-h-screen">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-5 py-6 sm:px-8 lg:px-10">
        <header className="flex flex-wrap items-center justify-between gap-3 rounded-full border border-line/80 bg-surface px-5 py-3 backdrop-blur-sm">
          <div>
            <p className="font-mono text-xs uppercase tracking-[0.32em] text-foreground/65">
              Spendly
            </p>
            <p className="text-sm text-foreground/70">
              Spending Advisor
            </p>
          </div>
          <Link
            className="rounded-full border border-line bg-surface-strong px-5 py-2.5 text-sm font-semibold text-foreground/80 transition-colors hover:bg-white"
            href="/"
          >
            Back to overview
          </Link>
        </header>

        <MvpConsole />
      </div>
    </main>
  );
}
