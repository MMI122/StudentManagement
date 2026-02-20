# Student Management System - Feature Updates

## Summary of Changes

All the requested real-life workflow features have been implemented:

### 1. ✅ Fixed TEST Course Auto-Creation Issue
**Problem:** A TEST course was being created automatically during login for role detection
**Solution:** Changed role detection logic to use DELETE endpoint test instead of POST (creating course)

### 2. ✅ Department-to-Course Relationship (1:M)
**Added:** One Department can have Many Courses
- Updated `Department.java` entity to include `courses` list
- Updated `Course.java` entity to include `department` reference
- Updated `CourseDto.java` to accept `departmentId` when creating courses

### 3. ✅ Department Dropdown When Creating Courses
**Feature:** Teachers can now select a department when creating a course
- Added department dropdown in the course creation form
- Backend validates and assigns the selected department to the course
- Courses table now shows which department each course belongs to

### 4. ✅ Assign Students to Departments
**Feature:** Teachers can assign students to departments using a dropdown
- Added "Assign Dept" button for each student (teacher-only)
- Modal popup appears with department dropdown
- Students table shows current department assignment
- Backend updates student's department via PUT endpoint

### 5. ✅ Department-Based Course Enrollment Restriction
**Feature:** Students can only enroll in courses from their assigned department
**Business Logic:**
- Student must be assigned to a department before enrolling
- Course must belong to a department
- Student can only enroll in courses from their department
- Clear error messages if validation fails

## Real-Life Workflow

### For Teachers:
1. **Create Departments** (e.g., Computer Science, Mathematics)
2. **Create Courses** and assign them to departments
3. **Assign Students** to departments
4. **Manage** students and courses

### For Students:
1. **View their assigned department**
2. **See only relevant courses** from their department
3. **Enroll in courses** (restricted to their department)
4. **Cannot delete** their own account (only teachers can)

## API Endpoints Updated

### Course Creation
```
POST /api/courses
Body: {
  "courseCode": "CS101",
  "courseName": "Introduction to Programming",
  "description": "Learn Java basics",
  "credits": 3,
  "departmentId": 1  // NEW: Assign to department
}
```

### Assign Student to Department
```
PUT /api/students/{studentId}/department/{departmentId}
```

### Enroll in Course (with department validation)
```
POST /api/students/{studentId}/courses/{courseId}
```

## Database Schema Changes

### Course Table
- Added `department_id` foreign key column

### Relationships
- Department (1) → Students (M) ✅
- Department (1) → Courses (M) ✅ NEW
- Student (M) ↔ Course (M) ✅
- Teacher (1) → Courses (M) ✅

## Testing the Features

### As a Teacher (username: ratul, password: 1234):
1. Login
2. Go to **Departments** → Create "Computer Science" department
3. Go to **Courses** → Create course with department dropdown
4. Go to **Students** → Click "Assign Dept" to assign students to departments
5. Delete courses/students if needed

### As a Student (username: rahul, password: 1234):
1. Login
2. Go to **Courses** → See only courses from your department
3. Click **Enroll** → Only works if:
   - You have a department assigned
   - Course belongs to your department
4. Cannot delete your own account (403 Forbidden)

## Error Messages

- **No department assigned:** "Student must be assigned to a department before enrolling in courses"
- **Course not in department:** "Course is not assigned to any department"
- **Wrong department:** "You can only enroll in courses from your department (Computer Science)"

## Next Steps

1. Start the application: `mvnw.cmd spring-boot:run`
2. Open browser: http://localhost:9090
3. Test the complete workflow:
   - Teacher creates departments and courses
   - Teacher assigns students to departments
   - Student can only enroll in department courses
