import { NextResponse } from "next/server";
import { createSupabaseServerClient } from "@/lib/supabase/server";

const maxOtpEmailRequests = 2;
const otpEmailRateLimitMs = 60_000;

type OtpRateLimitEntry = {
  count: number;
  resetAt: number;
};

const otpEmailRateLimits = new Map<string, OtpRateLimitEntry>();

function getRateLimitKey(email: string, request: Request) {
  const forwardedFor = request.headers.get("x-forwarded-for") ?? "local";
  return `${email.trim().toLowerCase()}:${forwardedFor}`;
}

export async function POST(request: Request) {
  const { email } = (await request.json()) as {
    email?: string;
  };

  if (!email) {
    return NextResponse.json(
      { error: "Email is required." },
      { status: 400 },
    );
  }

  const now = Date.now();
  const rateLimitKey = getRateLimitKey(email, request);
  const currentLimit = otpEmailRateLimits.get(rateLimitKey);
  const limit =
    currentLimit && currentLimit.resetAt > now
      ? currentLimit
      : { count: 0, resetAt: now + otpEmailRateLimitMs };

  if (limit.count >= maxOtpEmailRequests) {
    const resetInSeconds = Math.max(1, Math.ceil((limit.resetAt - now) / 1000));
    const error = `Too many sign-in emails. Try again in ${resetInSeconds}s.`;

    return NextResponse.json(
      {
        error,
        resetAt: limit.resetAt,
        resetInSeconds,
      },
      { status: 429 },
    );
  }

  const requestUrl = new URL(request.url);
  const supabase = await createSupabaseServerClient();
  const { error } = await supabase.auth.signInWithOtp({
    email,
    options: {
      emailRedirectTo: `${requestUrl.origin}/auth/callback`,
    },
  });

  if (error) {
    const isRateLimitError = /rate|limit|too many/i.test(error.message);

    return NextResponse.json(
      {
        error: isRateLimitError
          ? "Too many sign-in emails. Please wait a bit before trying again."
          : error.message,
      },
      { status: 400 },
    );
  }

  const nextLimit = {
    count: limit.count + 1,
    resetAt: limit.resetAt,
  };
  otpEmailRateLimits.set(rateLimitKey, nextLimit);

  return NextResponse.json({
    ok: true,
    requestCount: nextLimit.count,
    requestLimit: maxOtpEmailRequests,
    resetAt: nextLimit.resetAt,
  });
}
