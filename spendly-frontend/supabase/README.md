# Supabase Database

This folder has two kinds of SQL files:

- `migrations/` - SQL changes to run against Supabase, in order.
- `schema.sql` - a readable snapshot of what the database should look like after all migrations.

The important thing: files in this repo do not update Supabase by themselves. A migration only affects Supabase after it is applied through the Supabase SQL Editor, Supabase CLI, or the Supabase connector.

## What To Run

For the current app, there is one migration:

```text
supabase/migrations/202605250001_normalize_spendly_schema.sql
```

Run it once in Supabase.

It creates or normalizes these tables:

- `profiles`
- `purchase_checks`

It also enables row level security and user-owned access policies.

## Why The Filename Is Long

The filename starts with a timestamp-like version:

```text
202605250001
```

That keeps future database changes ordered.

The rest describes the change:

```text
normalize_spendly_schema
```

## Future Changes

When the database changes again, add a new file instead of editing the old migration:

```text
YYYYMMDDNNNN_short_description.sql
```

Examples:

```text
202605250001_normalize_spendly_schema.sql
202605250002_add_profile_currency.sql
```

Then run only the new migration in Supabase.
