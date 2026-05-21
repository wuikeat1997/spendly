create table if not exists public.ringly_profiles (
  user_id uuid primary key,
  monthly_income numeric not null default 0,
  monthly_commitments numeric not null default 0,
  current_balance numeric not null default 0,
  protected_buffer numeric not null default 300,
  last_balance_update timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.ringly_purchase_checks (
  id uuid primary key,
  user_id uuid not null,
  amount numeric not null,
  verdict text not null,
  consequence text not null,
  checked_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

alter table public.ringly_profiles
  add constraint ringly_profiles_user_id_fkey
  foreign key (user_id) references auth.users (id) on delete cascade;

alter table public.ringly_purchase_checks
  add constraint ringly_purchase_checks_user_id_fkey
  foreign key (user_id) references auth.users (id) on delete cascade;

alter table public.ringly_profiles enable row level security;
alter table public.ringly_purchase_checks enable row level security;

create policy "users_manage_own_profile"
on public.ringly_profiles
for all
using (auth.uid() = user_id)
with check (auth.uid() = user_id);

create policy "users_manage_own_purchase_checks"
on public.ringly_purchase_checks
for all
using (auth.uid() = user_id)
with check (auth.uid() = user_id);
