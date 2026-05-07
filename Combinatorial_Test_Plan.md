# Combinatorial Test Plan

## Purpose
This document defines combinatorial test cases for the project, focusing on key API flows and high-impact combinations of inputs.

## Scope
Covers combinations for:
- user signup
- user signin
- appointment booking
- doctor availability management
- doctor availability lookup

## Test Strategy
Use combinatorial testing to cover:
- pairs of related input conditions (pairwise)
- known invalid value combinations
- valid and invalid role interactions

---

## 1. User Signup Combinatorial Cases

### Input factors
- username: valid, blank, duplicate
- password: valid, blank
- fullName: valid, blank

### Selected combinations
| Case | username | password | fullName | Expected result |
|---|---|---|---|---|
| 1 | newuser | pass123 | Test User | success |
| 2 | `` | pass123 | Test User | fail: username blank |
| 3 | admin | pass123 | Test User | fail: duplicate username |
| 4 | newuser | `` | Test User | fail: password blank |
| 5 | newuser | pass123 | `` | fail: fullName blank |

---

## 2. User Signin Combinatorial Cases

### Input factors
- username: valid, blank, invalid
- password: valid, blank, invalid

### Selected combinations
| Case | username | password | Expected result |
|---|---|---|---|
| 1 | admin | admin123 | success |
| 2 | `` | admin123 | fail: missing username |
| 3 | admin | `` | fail: missing password |
| 4 | admin | wrongpass | fail: invalid credentials |
| 5 | unknown | pass123 | fail: invalid credentials |

---

## 3. Appointment Booking Combinatorial Cases

### Input factors
- username: existing patient, non-existing user
- doctorId: valid doctor, invalid doctor
- date/time: valid slot, invalid slot, missing values
- doctor availability state: empty availability, explicit availability

### Selected combinations
| Case | username | doctorId | date | time | doctor availability | Expected result |
|---|---|---|---|---|---|---|
| 1 | admin | 1 | 2026-06-01 | 10:00 | empty | success |
| 2 | unknown | 1 | 2026-06-01 | 10:00 | empty | fail: user missing |
| 3 | admin | 99 | 2026-06-01 | 10:00 | empty | fail: doctor missing |
| 4 | admin | 1 | `` | 10:00 | empty | fail: missing date |
| 5 | admin | 1 | 2026-06-01 | `` | empty | fail: missing time |
| 6 | admin | 1 | 2026-06-01 | 09:00 | slot unavailable | fail: slot unavailable |

> Note: For `slot unavailable`, pre-populate doctor 1 availability with a slot that is not available for the requested date/time.

---

## 4. Doctor Availability Management Combinatorial Cases

### Input factors
- username: doctor user, admin user, missing
- doctorId: valid doctor, invalid doctor, zero
- availability payload: valid list, empty list

### Selected combinations
| Case | username | userType | doctorId | availability | Expected result |
|---|---|---|---|---|---|
| 1 | edward | doctor | 1 | valid slots | success |
| 2 | admin | admin | 1 | valid slots | fail: only doctor may set availability |
| 3 | edward | doctor | 99 | valid slots | fail: doctor not found |
| 4 | `` | doctor | 1 | valid slots | fail: missing username |
| 5 | edward | doctor | 0 | valid slots | fail: invalid doctorId |

---

## 5. Doctor Availability Lookup Combinatorial Cases

### Input factors
- doctorId: valid, invalid
- availability state: populated, empty

### Selected combinations
| Case | doctorId | availability present | Expected result |
|---|---|---|---|
| 1 | 1 | empty | success: empty list |
| 2 | 1 | populated | success: availability list returned |
| 3 | 99 | n/a | fail: doctor not found |

---
