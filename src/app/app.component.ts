import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';

interface Doctor {
  id: number;
  name: string;
  department: string;
  bio: string;
}

interface User {
  username: string;
  fullName: string;
  userType?: string;
}

interface Appointment {
  username: string;
  doctorName: string;
  doctorId: number;
  date: string;
  time: string;
  department: string;
}

interface AvailabilitySlot {
  date: string;
  time: string;
  isAvailable: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  activeSection: 'home' | 'about' | 'signin' | 'appointments' | 'doctor-dashboard' = 'home';
  showSignUp = false;
  user: User | null = null;
  successMessage = '';
  errorMessage = '';
  appointmentMessage = '';

  signIn = {
    username: '',
    password: ''
  };

  signUp = {
    username: '',
    fullName: '',
    password: ''
  };

  appointment = {
    doctorId: 1,
    date: '',
    time: ''
  };

  userAppointments: Appointment[] = [];
  doctors: Doctor[] = [];
  selectedDoctor: Doctor | null = null;
  private readonly apiBase = 'http://localhost:8080/api';

  // Doctor availability management
  availabilitySlots: AvailabilitySlot[] = [];
  newAvailability = {
    date: '',
    time: '',
    isAvailable: true
  };

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadDoctors();
    this.checkStoredUser();
  }

  selectSection(section: 'home' | 'about' | 'signin' | 'appointments' | 'doctor-dashboard'): void {
    this.activeSection = section;
    this.successMessage = '';
    this.errorMessage = '';
    this.appointmentMessage = '';
    
    // Load appointments when entering appointments section for patients
    if (section === 'appointments' && this.user && this.user.userType === 'patient') {
      this.loadUserAppointments();
    }
    
    // Load doctor dashboard if entering doctor section
    if (section === 'doctor-dashboard' && this.user && this.user.userType === 'doctor') {
      this.loadDoctorAvailability();
    }
  }

  toggleSignMode(value: boolean): void {
    this.showSignUp = value;
    this.errorMessage = '';
    this.successMessage = '';
  }

  loadDoctors(): void {
    this.http.get<Doctor[]>(`${this.apiBase}/doctors`).subscribe({
      next: (result) => {
        this.doctors = result;
        if (!this.doctors.length) {
          this.doctors = this.getDefaultDoctors();
        }
      },
      error: () => {
        this.doctors = this.getDefaultDoctors();
      }
    });
  }

  loadUserAppointments(): void {
    if (!this.user) return;
    this.http.get<Appointment[]>(`${this.apiBase}/appointments?username=${this.user.username}`).subscribe({
      next: (appointments) => {
        this.userAppointments = appointments;
      },
      error: (error) => {
        console.error('Error loading appointments:', error);
        this.userAppointments = [];
      }
    });
  }

  loadDoctorAvailability(): void {
    if (!this.user) return;
    // Assuming the doctor ID is 1 for now (you may need to get this from user data)
    const doctorId = 1;
    this.http.get<any[]>(`${this.apiBase}/doctors/availability?doctorId=${doctorId}`).subscribe({
      next: (slots) => {
        this.availabilitySlots = slots;
      },
      error: (error) => {
        console.error('Error loading availability:', error);
        this.availabilitySlots = [];
      }
    });
  }

  getDefaultDoctors(): Doctor[] {
    return [
      { id: 1, name: 'Dr. Maya Patel', department: 'Cardiology', bio: 'Heart care specialist with 12 years of experience.' },
      { id: 2, name: 'Dr. Kevin Thompson', department: 'Neurology', bio: 'Expert in brain and nervous system treatment.' },
      { id: 3, name: 'Dr. Aisha Rahman', department: 'Pediatrics', bio: 'Child health doctor with a caring bedside manner.' },
      { id: 4, name: 'Dr. Samuel Kim', department: 'Orthopedics', bio: 'Bone and joint specialist for adults and seniors.' },
      { id: 5, name: 'Dr. Elena Garcia', department: 'Dermatology', bio: 'Skin and allergy doctor for comprehensive skin care.' }
    ];
  }

  checkStoredUser(): void {
    const storedUser = localStorage.getItem('hospitalUser');
    if (storedUser) {
      try {
        const userData = JSON.parse(storedUser);
        this.user = userData;
        if (this.user && this.user.userType === 'patient') {
          this.loadUserAppointments();
        }
      } catch (error) {
        localStorage.removeItem('hospitalUser');
      }
    }
  }

  signInUser(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.signIn.username || !this.signIn.password) {
      this.errorMessage = 'Please enter both username and password.';
      return;
    }

    this.http.post<{ fullName: string; welcomeMessage: string; userType?: string }>(`${this.apiBase}/auth/signin`, this.signIn).subscribe({
      next: (response) => {
        this.user = {
          username: this.signIn.username,
          fullName: response.fullName,
          userType: response.userType || 'patient'
        };
        localStorage.setItem('hospitalUser', JSON.stringify(this.user));
        this.successMessage = response.welcomeMessage;
        this.errorMessage = '';
        
        // Route to appropriate section based on user type
        if (this.user.userType === 'doctor') {
          this.activeSection = 'doctor-dashboard';
          this.loadDoctorAvailability();
        } else {
          this.activeSection = 'appointments';
          this.loadUserAppointments();
        }
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Sign in failed. Please check your credentials.';
      }
    });
  }

  signUpUser(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.signUp.username || !this.signUp.password || !this.signUp.fullName) {
      this.errorMessage = 'All fields are required to sign up.';
      return;
    }

    this.http.post<{ message: string; fullName: string }>(`${this.apiBase}/auth/signup`, this.signUp).subscribe({
      next: (response) => {
        this.user = {
          username: this.signUp.username,
          fullName: this.signUp.fullName,
          userType: 'patient'
        };
        localStorage.setItem('hospitalUser', JSON.stringify(this.user));
        this.successMessage = response.message;
        this.errorMessage = '';
        this.activeSection = 'appointments';
        this.loadUserAppointments();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Signup failed. Please try a different username.';
      }
    });
  }

  selectDoctor(doctor: Doctor): void {
    this.selectedDoctor = doctor;
    this.appointment.doctorId = doctor.id;
    this.appointmentMessage = '';
  }

  makeAppointment(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.appointmentMessage = '';
    if (!this.user) {
      this.errorMessage = 'Please sign in before booking an appointment.';
      return;
    }
    if (!this.appointment.date || !this.appointment.time) {
      this.errorMessage = 'Please choose a date and time for your appointment.';
      return;
    }
    const details = {
      username: this.user.username,
      doctorId: this.appointment.doctorId,
      date: this.appointment.date,
      time: this.appointment.time
    };

    this.http.post<{ message: string }>(`${this.apiBase}/appointments`, details).subscribe({
      next: (response) => {
        this.appointmentMessage = response.message;
        this.selectedDoctor = null;
        this.appointment = { doctorId: 1, date: '', time: '' };
        this.loadUserAppointments();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Unable to create appointment. Try again later.';
      }
    });
  }

  addAvailabilitySlot(): void {
    if (!this.newAvailability.date || !this.newAvailability.time) {
      this.errorMessage = 'Please select both date and time.';
      return;
    }
    
    const slot: AvailabilitySlot = {
      date: this.newAvailability.date,
      time: this.newAvailability.time,
      isAvailable: this.newAvailability.isAvailable
    };
    
    this.availabilitySlots.push(slot);
    this.newAvailability = { date: '', time: '', isAvailable: true };
    this.successMessage = 'Availability slot added successfully!';
  }

  saveAvailability(): void {
    if (!this.user) {
      this.errorMessage = 'Please sign in first.';
      return;
    }

    const payload = {
      username: this.user.username,
      doctorId: 1, // Assuming doctor ID 1 for now
      availability: this.availabilitySlots
    };

    this.http.post<{ message: string; success: boolean }>(`${this.apiBase}/doctors/availability`, payload).subscribe({
      next: (response) => {
        this.successMessage = response.message || 'Availability updated successfully!';
        this.errorMessage = '';
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Failed to save availability.';
      }
    });
  }

  removeAvailabilitySlot(index: number): void {
    this.availabilitySlots.splice(index, 1);
  }

  signOut(): void {
    this.user = null;
    localStorage.removeItem('hospitalUser');
    this.activeSection = 'home';
    this.successMessage = '';
    this.errorMessage = '';
    this.appointmentMessage = '';
    this.showSignUp = false;
    this.signIn = { username: '', password: '' };
    this.signUp = { username: '', password: '', fullName: '' };
    this.userAppointments = [];
    this.availabilitySlots = [];
  }
}
