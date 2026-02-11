-- USERS
create table app_user (
  id uuid primary key,
  role varchar(20) not null,
  first_name varchar(80) not null,
  last_name varchar(80) not null,
  username varchar(60) not null unique,
  email varchar(120) not null unique,
  password_hash varchar(255) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

-- PROJECTS
create table project (
  id uuid primary key,
  name varchar(160) not null,
  description text,
  owner_id uuid not null references app_user(id),
  created_at timestamptz not null,
  updated_at timestamptz not null
);

-- TASKS
create table task (
  id uuid primary key,
  project_id uuid not null references project(id) on delete cascade,
  name varchar(200) not null,
  description text,
  start_date date,
  end_date date,
  status varchar(30) not null,
  progress int not null,
  created_at timestamptz not null,
  updated_at timestamptz not null
);

-- PROJECT MEMBERS (people assigned to project)
create table project_member (
  project_id uuid not null references project(id) on delete cascade,
  user_id uuid not null references app_user(id) on delete cascade,
  workload_percent int not null,
  overtime_hours numeric(10,2) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  primary key (project_id, user_id)
);

-- OPTIONAL: TASK ASSIGNEES
create table task_assignee (
  task_id uuid not null references task(id) on delete cascade,
  user_id uuid not null references app_user(id) on delete cascade,
  assigned_hours numeric(10,2) not null,
  overtime_hours numeric(10,2) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  primary key (task_id, user_id)
);
