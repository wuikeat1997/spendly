import Link from "next/link";

const releaseNotes = [
  "Magic-link sign-in is available once your Supabase environment variables are configured.",
  "Profile and purchase checks persist through the active adapter.",
  "The app route now separates product usage from the landing page.",
  "Supabase schema and row-level policies are ready for setup.",
];

const persistenceSteps = [
  "Local storage when no Supabase config is present",
  "Email magic link when Supabase is configured",
  "Profile row plus purchase-check rows in Postgres",
  "Ready for a later move to server-side auth guards",
];

export default function ReleaseHistoryPage() {
  return (
    <main className="grain min-h-screen">
      <div className="mx-auto flex w-full max-w-5xl flex-col gap-8 px-5 py-6 sm:px-8 lg:px-10">
        <header className="flex flex-wrap items-center justify-between gap-3 rounded-full border border-line/80 bg-surface px-5 py-3 backdrop-blur-sm">
          <div>
            <p className="font-mono text-xs uppercase tracking-[0.32em] text-foreground/65">
              Release History
            </p>
            <p className="text-sm text-foreground/70">
              Product and platform changes for the safe-to-spend workspace
            </p>
          </div>
          <Link
            className="rounded-full border border-line bg-surface-strong px-5 py-2.5 text-sm font-semibold text-foreground/80 transition-colors hover:bg-white"
            href="/app"
          >
            Back to /app
          </Link>
        </header>

        <section className="rounded-[2rem] border border-line bg-surface px-6 py-7 shadow-[0_20px_80px_rgba(112,77,37,0.08)]">
          <p className="font-mono text-xs uppercase tracking-[0.28em] text-foreground/55">
            Current Layer
          </p>
          <h1 className="mt-3 text-4xl font-semibold tracking-[-0.05em] text-foreground">
            What This Layer Adds
          </h1>
          <p className="mt-4 max-w-3xl text-base leading-7 text-foreground/72">
            This release moves the MVP from a local interactive prototype into a
            Supabase-ready app structure with route separation, persistence
            adapters, and authentication hooks.
          </p>

          <div className="mt-6 grid gap-3">
            {releaseNotes.map((item) => (
              <div
                key={item}
                className="rounded-[1.2rem] border border-line bg-[#fffdfa] px-4 py-4 text-sm leading-6 text-foreground/72"
              >
                {item}
              </div>
            ))}
          </div>
        </section>

        <section className="rounded-[2rem] border border-line bg-[#fff7ea] px-6 py-7">
          <p className="font-mono text-xs uppercase tracking-[0.28em] text-accent-deep">
            Persistence Path
          </p>
          <div className="mt-5 grid gap-3">
            {persistenceSteps.map((item) => (
              <div
                key={item}
                className="rounded-[1.2rem] border border-[#e8d7bc] bg-[#fffaf0] px-4 py-4 text-sm leading-6 text-[#5b4031]"
              >
                {item}
              </div>
            ))}
          </div>
        </section>
      </div>
    </main>
  );
}
