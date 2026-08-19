create table if not exists user_accounts (
    id uuid constraint user_accounts_pk primary key,
    email varchar(255) not null constraint uk_user_account_email unique,
    password_hash varchar(255) not null,
    role varchar(255) not null constraint ck_user_accounts_role check (role in ('STUDENT', 'TEACHER', 'ADMIN')),
    is_active boolean not null
);

create table if not exists promotion (
    id uuid constraint promotion_pk primary key,
    year integer not null constraint uk_promotion_year unique
);

create table if not exists groups (
    id uuid constraint groups_pk primary key,
    ref varchar(255) not null,
    track varchar(255) not null constraint ck_groups_track check (track in ('COMMON', 'EL', 'TN'))
);

create table if not exists students (
    id uuid constraint students_pk primary key,
    std varchar(255) not null constraint uk_student_std unique,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    promotion_id uuid not null constraint fk_students_promotion references promotion (id),
    user_account_id uuid not null constraint uk_students_user_account unique constraint fk_students_user_account references user_accounts (id)
);

create table if not exists teachers (
    id uuid constraint teachers_pk primary key,
    first_name varchar(255) not null,
    last_name varchar(255) not null,
    user_account_id uuid not null constraint uk_teachers_user_account unique constraint fk_teachers_user_account references user_accounts (id)
);

create table if not exists student_group_histories (
    id uuid constraint student_group_histories_pk primary key,
    student_id uuid not null constraint fk_student_group_histories_student references students (id),
    group_id uuid not null constraint fk_student_group_histories_group references groups (id),
    start_date timestamptz not null,
    end_date timestamptz,
    assigned_by_id uuid not null constraint fk_student_group_histories_assigned_by references user_accounts (id)
);

create table if not exists courses (
    id uuid constraint courses_pk primary key,
    ref varchar(255) not null constraint uk_course_ref unique,
    title varchar(255) not null,
    credits integer not null
);

create table if not exists course_assignments (
    id uuid constraint course_assignments_pk primary key,
    course_id uuid not null constraint fk_course_assignments_course references courses (id),
    teacher_id uuid not null constraint fk_course_assignments_teacher references teachers (id),
    group_id uuid not null constraint fk_course_assignments_group references groups (id),
    semester varchar(255) not null constraint ck_course_assignments_semester check (semester in ('S1', 'S2', 'S3', 'S4', 'S5', 'S6')),
    academic_year integer not null
);

create table if not exists exams (
    id uuid constraint exams_pk primary key,
    date_exam timestamptz not null,
    coefficient numeric(5, 4) not null,
    course_id uuid not null constraint fk_exams_course references courses (id)
);

create table if not exists grades (
    id uuid constraint grades_pk primary key,
    student_id uuid not null constraint fk_grades_student references students (id),
    exam_id uuid not null constraint fk_grades_exam references exams (id),
    value numeric(5, 2) not null,
    constraint uk_grade_student_exam unique (student_id, exam_id)
);

create table if not exists grade_histories (
    id uuid constraint grade_histories_pk primary key,
    grade_id uuid not null constraint fk_grade_histories_grade references grades (id),
    old_value numeric(5, 2) not null,
    new_value numeric(5, 2) not null,
    reason varchar(500) not null,
    modified_by_id uuid not null constraint fk_grade_histories_modified_by references user_accounts (id),
    modified_at timestamptz not null
);