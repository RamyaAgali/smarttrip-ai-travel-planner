import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import App from './App.jsx'
import Signup from './Signup.jsx'
import Dashboard from './Dashboard.jsx'
import ForgotPassword from './Forgotpassword.jsx'
import ResetPassword from './ResetPassword.jsx'
import TravelAssistant from './TravelAssistant.jsx'
import Profile from './Profile.jsx'
import MyTrips from './MyTrips.jsx'
import BackToDashboardButton from './components/BackToDashboardButton.jsx'
import PaymentPage from './PaymentPage.jsx'
import PaymentSuccess from './PaymentSuccess.jsx'
import PaymentFailure from './PaymentFailure.jsx'
import ProtectedLayout from './components/ProtectedLayout.jsx'
import PaymentStatus from './PaymentStatus.jsx'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>

        {/* PUBLIC ROUTES (No Chatbot) */}
        <Route path="/" element={<App />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* PROTECTED ROUTES (Chatbot visible) */}
        <Route element={<ProtectedLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/Profile" element={<Profile />} />
          <Route path="/my-trips" element={<MyTrips />} />
          <Route path="/travel-assistant" element={<TravelAssistant />} />
          <Route path="/Back-to-dashboard" element={<BackToDashboardButton />} />
          <Route path="/payment/:tripId" element={<PaymentPage />} />
          <Route path="/payment/success" element={<PaymentSuccess />} />
          <Route path="/payment/failure" element={<PaymentFailure />} />
          <Route path="/payment/status" element={<PaymentStatus/>}/>
        </Route>

      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
)
//force deploy