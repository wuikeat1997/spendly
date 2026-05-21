export default function Home() {
  return (
    <main className="grain min-h-screen">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-5 py-6 sm:px-8 lg:px-10">
        <header className="flex items-center justify-between rounded-full border border-line/80 bg-surface px-5 py-3 backdrop-blur-sm">
          <div>
            <p className="font-mono text-xs uppercase tracking-[0.32em] text-foreground/65">
              Spendly
            </p>
            <p className="text-sm text-foreground/70">
              Safe-to-spend control system
            </p>
          </div>
          <div className="rounded-full border border-signal-safe/25 bg-signal-safe/10 px-4 py-2 font-mono text-xs uppercase tracking-[0.22em] text-signal-safe">
            Approximate. Fast. Honest.
          </div>
        </header>

        <section className="grid gap-6 lg:grid-cols-[1.15fr_0.85fr]">
          <div className="rounded-[2rem] border border-line bg-surface px-6 py-8 shadow-[0_20px_80px_rgba(112,77,37,0.08)] sm:px-8 sm:py-10">
            <div className="mb-10 flex flex-wrap gap-3">
              <span className="rounded-full bg-accent/12 px-4 py-2 font-mono text-xs uppercase tracking-[0.24em] text-accent-deep">
                Before You Pay
              </span>
              <span className="rounded-full bg-mint/18 px-4 py-2 font-mono text-xs uppercase tracking-[0.24em] text-signal-safe">
                Control Over Tracking
              </span>
            </div>

            <h1 className="max-w-4xl text-5xl leading-none font-semibold tracking-[-0.06em] text-foreground sm:text-6xl lg:text-7xl">
              Stop guessing. Know whether a spend is safe in the five seconds
              before it happens.
            </h1>

            <p className="mt-6 max-w-2xl text-lg leading-8 text-foreground/72 sm:text-xl">
              Spendly is built for salary earners living close to the edge of
              their monthly buffer. Instead of showing charts after the damage,
              it gives a fast pre-spend verdict based on what is left, what is
              protected, and how many days remain.
            </p>

            <div className="mt-8 flex flex-wrap gap-3">
              <a
                className="rounded-full bg-accent px-6 py-3 text-sm font-semibold text-[#fff8f2] transition-transform hover:-translate-y-0.5"
                href="/app"
              >
                Open the app
              </a>
              <a
                className="rounded-full border border-line bg-surface-strong px-6 py-3 text-sm font-semibold text-foreground/85 transition-colors hover:bg-white"
                href="#rules"
              >
                Review Decision Rules
              </a>
            </div>
          </div>

          <div
            id="prototype"
            className="rounded-[2rem] border border-line bg-[#13261a] p-5 text-[#f7f1e5] shadow-[0_24px_90px_rgba(19,38,26,0.22)]"
          >
            <div className="rounded-[1.6rem] border border-white/10 bg-white/5 p-5">
              <p className="font-mono text-[11px] uppercase tracking-[0.28em] text-[#d4ddc6]">
                Product Promise
              </p>
              <h2 className="mt-4 text-4xl font-semibold tracking-[-0.06em] text-[#f8f2e7]">
                Better decisions under pressure.
              </h2>
              <p className="mt-4 text-base leading-7 text-[#d5dbcf]">
                The product is not trying to become a reporting dashboard. It is
                trying to become the thing a user checks right before a spend so
                they stop making blind decisions.
              </p>

              <div className="mt-5 grid gap-3">
                {[
                  "Free tier: know if you are safe.",
                  "Paid tier later: stay in control automatically.",
                  "Trust comes from speed, accuracy, and non-judgment.",
                ].map((line) => (
                  <div
                    key={line}
                    className="rounded-[1.2rem] border border-white/10 bg-white/6 px-4 py-4 text-sm leading-6 text-[#e6eadf]"
                  >
                    {line}
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="grid gap-5 lg:grid-cols-3">
          {[
            {
              eyebrow: "1. Setup",
              title: "Three inputs, not a spreadsheet",
              body:
                "Income, commitments, and rough current balance. That is enough to start giving directionally correct guidance.",
            },
            {
              eyebrow: "2. Decide",
              title: "One-tap pre-spend check",
              body:
                "Enter the purchase amount and get a fast answer: safe, risky, or not safe, with the consequence stated plainly.",
            },
            {
              eyebrow: "3. Recover",
              title: "Short rescue plans instead of shame",
              body:
                "When the user drifts, the product switches to a low-cognitive-load recovery plan built to restore control quickly.",
            },
          ].map((item) => (
            <article
              key={item.title}
              className="rounded-[1.6rem] border border-line bg-surface px-5 py-6"
            >
              <p className="font-mono text-[11px] uppercase tracking-[0.24em] text-foreground/55">
                {item.eyebrow}
              </p>
              <h2 className="mt-4 text-2xl font-semibold tracking-[-0.04em]">
                {item.title}
              </h2>
              <p className="mt-3 text-base leading-7 text-foreground/70">
                {item.body}
              </p>
            </article>
          ))}
        </section>

        <section className="grid gap-6 lg:grid-cols-[0.9fr_1.1fr]">
          <div className="rounded-[1.8rem] border border-line bg-surface px-6 py-7">
            <p className="font-mono text-xs uppercase tracking-[0.28em] text-foreground/55">
              Target User
            </p>
            <h2 className="mt-4 text-3xl font-semibold tracking-[-0.05em]">
              Aware. Slightly struggling. Trying to be better.
            </h2>
            <p className="mt-4 text-base leading-7 text-foreground/72">
              The initial wedge is salary earners living month to month and
              young professionals with recurring tradeoffs between lifestyle,
              savings, and obligations. The product is built for repeated
              “can I afford this?” moments, not end-of-month analysis.
            </p>
          </div>

          <div
            id="rules"
            className="rounded-[1.8rem] border border-line bg-[#fff7ea] px-6 py-7"
          >
            <p className="font-mono text-xs uppercase tracking-[0.28em] text-accent-deep">
              Core Rule Engine
            </p>
            <div className="mt-4 rounded-[1.4rem] bg-[#1e3024] p-5 font-mono text-sm leading-7 text-[#dce8d7]">
              <p>spendable_balance = current_balance - protected_buffer</p>
              <p>safe_daily = spendable_balance / remaining_days</p>
              <p className="mt-4 text-[#f9c3ab]">
                if purchase_amount &lt;= safe_daily -&gt; SAFE
              </p>
              <p className="text-[#f1de9e]">
                if purchase_amount &gt; safe_daily -&gt; RISKY
              </p>
              <p className="text-[#ffb09a]">
                if protected money breaks -&gt; NOT SAFE
              </p>
            </div>
            <p className="mt-4 text-base leading-7 text-foreground/72">
              The first version favors transparent approximation over fake
              precision. If balance data is stale, the app asks for a quick
              refresh instead of pretending confidence.
            </p>
          </div>
        </section>
      </div>
    </main>
  );
}
