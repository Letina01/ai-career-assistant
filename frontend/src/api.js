import axios from 'axios';

const DEFAULT_API_BASE_URL = 'http://localhost:8080/api';

const normalizeApiBaseUrl = (rawValue) => {
  const value = (rawValue || '').trim();
  if (!value) {
    return DEFAULT_API_BASE_URL;
  }
  if (value.startsWith(':')) {
    return `http://localhost${value}`;
  }
  if (value.startsWith('//')) {
    return `${window.location.protocol}${value}`;
  }
  if (value.startsWith('/')) {
    return `${window.location.origin}${value}`;
  }
  if (!/^https?:\/\//i.test(value)) {
    return `http://${value}`;
  }
  return value;
};

const api = axios.create({
  baseURL: normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL).replace(/\/+$/, '')
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('career-token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const login = (payload) => api.post('/auth/login', payload).then((res) => res.data);
export const register = (payload) => api.post('/auth/register', payload).then((res) => res.data);
export const requestPasswordReset = (payload) => api.post('/auth/request-password-reset', payload).then((res) => res.data);
export const resetPassword = (payload) => api.post('/auth/reset-password', payload).then((res) => res.data);
export const fetchDashboard = () => api.get('/dashboard').then((res) => res.data);
export const fetchResumes = () => api.get('/resumes').then((res) => res.data);
export const uploadResume = (formData) =>
  api.post('/resumes/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then((res) => res.data);
export const fetchJobRecommendations = (resumeId, query, location) =>
  api.get('/jobs/recommend', { params: { resumeId, query, location } }).then((res) => res.data);
export const saveJob = (payload) => api.post('/jobs/save', payload).then((res) => res.data);
export const sendChatMessage = (payload) => api.post('/chat', payload).then((res) => res.data);
export const generateInterviewPrep = (payload) => api.post('/interview/prepare', payload).then((res) => res.data);
export const analyzeSkillGap = (payload) => api.post('/skill-gap', payload).then((res) => res.data);
export const improveResume = (payload) => api.post('/resume-improvements', payload).then((res) => res.data);
export const fetchApplicantJobs = () => api.get('/applicant/jobs').then((res) => res.data);
export const applyToJob = (jobId, payload) => api.post(`/applicant/jobs/${jobId}/apply`, payload).then((res) => res.data);
export const fetchMyApplications = () => api.get('/applicant/applications').then((res) => res.data);
export const createRecruiterJob = (payload) => api.post('/recruiter/jobs', payload).then((res) => res.data);
export const fetchRecruiterJobs = () => api.get('/recruiter/jobs').then((res) => res.data);
export const fetchRecruiterApplications = () => api.get('/recruiter/applications').then((res) => res.data);
export const updateApplicationStatus = (applicationId, payload) =>
  api.put(`/recruiter/applications/${applicationId}`, payload).then((res) => res.data);
export const downloadApplicantResume = (applicationId) =>
  api.get(`/recruiter/applications/${applicationId}/resume`, { responseType: 'blob' }).then((res) => ({
    blob: res.data,
    fileName: res.headers['content-disposition']?.split('filename=')[1]?.replace(/"/g, '') || 'resume.txt'
  }));
export const fetchProfile = () => api.get('/profile').then((res) => res.data);
export const updateProfile = (payload) => api.put('/profile', payload).then((res) => res.data);
