# Entity Relationship Diagram

```mermaid
erDiagram
  DEPARTMENTS ||--o{ STUDENTS : contains
  DEPARTMENTS ||--o{ FACULTY : employs
  DEPARTMENTS ||--o{ COURSES : offers
  FACULTY ||--o{ COURSES : teaches
  STUDENTS ||--o{ ENROLLMENTS : registers
  COURSES ||--o{ ENROLLMENTS : includes
  ENROLLMENTS ||--o{ ATTENDANCE : records
  COURSES ||--o{ ASSESSMENTS : defines
  ASSESSMENTS ||--o{ MARKS : awards
  STUDENTS ||--o{ MARKS : receives
```

The enrollment table resolves the many-to-many relationship between students and courses. Attendance belongs to a student's course enrollment, while marks connect a student to an assessment.
