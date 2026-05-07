# MC/DC Test Plan

## Purpose
This document defines Minimum Complexity/Decision Coverage (MC/DC) test cases for the backend API logic in `Backend/myapp/demo/src/main/java/com/example/demo/HospitalController.java`.

## Scope
Covers main decision logic for:
- `POST /api/auth/signup`
- `POST /api/auth/signin`
- `POST /api/appointments`
- `POST /api/doctors/availability`
- `GET /api/doctors/availability`

## Notation
- `T` = true
- `F` = false
- `✔` = expected success response
- `✖` = expected bad request or failure response

---

## 1. Sign Up: `POST /api/auth/signup`

### Decision logic
Returns error if any of these fail:
1. `username` is null or blank
2. `password` is null or blank
3. `fullName` is null or blank
4. `username` already exists

### Conditions
- `C1`: username valid
- `C2`: password valid
- `C3`: fullName valid
- `C4`: username unique

### MC/DC test cases
| Case | username | password | fullName | username unique | Expected result | Purpose |
|---|---|---|---|---|---|---|
| 1 | `newuser` | `pass123` | `Test User` | T | ✔ | all true baseline |
| 2 | `` | `pass123` | `Test User` | T | ✖ | C1 false isolates username blank |
| 3 | `newuser` | `` | `Test User` | T | ✖ | C2 false isolates password blank |
| 4 | `newuser` | `pass123` | `` | T | ✖ | C3 false isolates fullName blank |
| 5 | `admin` | `admin123` | `Hospital Admin` | F | ✖ | C4 false isolates duplicate username |

---

## 2. Sign In: `POST /api/auth/signin`

### Decision logic
Returns error if either of these fail:
1. `username` is null or blank
2. `password` is null or blank
3. credentials are invalid

### Conditions
- `C1`: username provided
- `C2`: password provided
- `C3`: credentials valid

### MC/DC test cases
| Case | username | password | credentials valid | Expected result | Purpose |
|---|---|---|---|---|---|
| 1 | `admin` | `admin123` | T | ✔ | baseline success |
| 2 | `` | `admin123` | F | ✖ | C1 false isolates missing username |
| 3 | `admin` | `` | F | ✖ | C2 false isolates missing password |
| 4 | `admin` | `wrongpass` | F | ✖ | C3 false isolates invalid credentials |

---

## 3. Book Appointment: `POST /api/appointments`

### Decision logic
Returns error if any of these fail:
1. all fields present and valid
2. user exists
3. doctor exists
4. selected slot is available (if availability list is non-empty)

### Conditions
- `C1`: all request fields complete
- `C2`: username is signed in / exists
- `C3`: doctor exists for `doctorId`
- `C4`: slot available or doctor has no availability restrictions

### MC/DC test cases
| Case | username | doctorId | date | time | user exists | doctor exists | slot available | Expected result | Purpose |
|---|---|---|---|---|---|---|---|---|---|
| 1 | `admin` | `1` | `2026-06-01` | `10:00` | T | T | T | ✔ | baseline success |
| 2 | `` | `1` | `2026-06-01` | `10:00` | F | T | T | ✖ | C1 false fields incomplete |
| 3 | `unknown` | `1` | `2026-06-01` | `10:00` | F | T | T | ✖ | C2 false user missing |
| 4 | `admin` | `99` | `2026-06-01` | `10:00` | T | F | T | ✖ | C3 false invalid doctor |
| 5 | `admin` | `1` | `2026-06-01` | `10:00` | T | T | F | ✖ | C4 false slot unavailable |

> Note: If doctor availability is empty, `slot available` is treated as true by the implementation.

---

## 4. Set Doctor Availability: `POST /api/doctors/availability`

### Decision logic
Returns error if:
1. `username` missing or blank
2. `doctorId` missing or invalid
3. user does not exist or is not a doctor
4. doctor does not exist

### Conditions
- `C1`: username valid
- `C2`: doctorId positive
- `C3`: user exists and userType == `doctor`
- `C4`: doctor exists

### MC/DC test cases
| Case | username | doctorId | userType | doctor exists | Expected result | Purpose |
|---|---|---|---|---|---|---|
| 1 | `edward` | `1` | doctor | T | ✔ | baseline success |
| 2 | `` | `1` | N/A | T | ✖ | C1 false missing username |
| 3 | `edward` | `0` | doctor | T | ✖ | C2 false invalid doctorId |
| 4 | `admin` | `1` | admin | T | ✖ | C3 false wrong user role |
| 5 | `edward` | `99` | doctor | F | ✖ | C4 false doctor not found |

---

## 5. Get Doctor Availability: `GET /api/doctors/availability`

### Decision logic
Returns error if doctor does not exist.

### Conditions
- `C1`: doctor exists

### MC/DC test cases
| Case | doctorId | Expected result | Purpose |
|---|---|---|---|
| 1 | `1` | ✔ | baseline doctor found |
| 2 | `99` | ✖ | C1 false doctor missing |

---

