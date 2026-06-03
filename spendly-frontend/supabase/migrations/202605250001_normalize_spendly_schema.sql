do $$
begin
  if to_regclass('public.profiles') is null
    and to_regclass('public.ringly_profiles') is not null then
    alter table public.ringly_profiles rename to profiles;
  end if;

  if to_regclass('public.purchase_checks') is null
    and to_regclass('public.ringly_purchase_checks') is not null then
    alter table public.ringly_purchase_checks rename to purchase_checks;
  end if;
end $$;

create table if not exists public.profiles (
  user_id uuid primary key,
  monthly_income numeric not null default 0,
  monthly_commitments numeric not null default 0,
  current_balance numeric not null default 0,
  protected_buffer numeric not null default 300,
  last_balance_update timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.purchase_checks (
  id uuid primary key,
  user_id uuid not null,
  amount numeric not null,
  verdict text not null,
  consequence text not null,
  checked_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

do $$
begin
  if exists (
    select 1
    from pg_constraint
    where conname = 'ringly_profiles_user_id_fkey'
      and conrelid = 'public.profiles'::regclass
  ) and not exists (
    select 1
    from pg_constraint
    where conname = 'profiles_user_id_fkey'
      and conrelid = 'public.profiles'::regclass
  ) then
    alter table public.profiles
      rename constraint ringly_profiles_user_id_fkey to profiles_user_id_fkey;
  end if;

  if not exists (
    select 1
    from pg_constraint
    where conname = 'profiles_user_id_fkey'
      and conrelid = 'public.profiles'::regclass
  ) then
    alter table public.profiles
      add constraint profiles_user_id_fkey
      foreign key (user_id) references auth.users (id) on delete cascade;
  end if;

  if exists (
    select 1
    from pg_constraint
    where conname = 'ringly_purchase_checks_user_id_fkey'
      and conrelid = 'public.purchase_checks'::regclass
  ) and not exists (
    select 1
    from pg_constraint
    where conname = 'purchase_checks_user_id_fkey'
      and conrelid = 'public.purchase_checks'::regclass
  ) then
    alter table public.purchase_checks
      rename constraint ringly_purchase_checks_user_id_fkey to purchase_checks_user_id_fkey;
  end if;

  if not exists (
    select 1
    from pg_constraint
    where conname = 'purchase_checks_user_id_fkey'
      and conrelid = 'public.purchase_checks'::regclass
  ) then
    alter table public.purchase_checks
      add constraint purchase_checks_user_id_fkey
      foreign key (user_id) references auth.users (id) on delete cascade;
  end if;
end $$;

alter table public.profiles enable row level security;
alter table public.purchase_checks enable row level security;

do $$
begin
  if not exists (
    select 1
    from pg_policies
    where schemaname = 'public'
      and tablename = 'profiles'
      and policyname = 'users_manage_own_profile'
  ) then
    create policy "users_manage_own_profile"
    on public.profiles
    for all
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);
  end if;

  if not exists (
    select 1
    from pg_policies
    where schemaname = 'public'
      and tablename = 'purchase_checks'
      and policyname = 'users_manage_own_purchase_checks'
  ) then
    create policy "users_manage_own_purchase_checks"
    on public.purchase_checks
    for all
    using (auth.uid() = user_id)
    with check (auth.uid() = user_id);
  end if;
end $$;
