// import React from 'react'
// import ReactDOM from 'react-dom/client'
// import { BrowserRouter, Routes, Route } from 'react-router-dom'
// import App from './App.jsx'
// import Signup from './Signup.jsx'
// import Dashboard from './Dashboard.jsx'
// import ForgotPassword from './Forgotpassword.jsx'
// import ResetPassword from './ResetPassword.jsx'
// import TravelAssistant from './TravelAssistant.jsx'
// import Profile from './profile.jsx'
// import MyTrips from './MyTrips.jsx'
// import BackToDashboardButton from './components/BackToDashboardButton.jsx'
// import PaymentPage from './PaymentPage.jsx'
// import AIAssistant from './components/AIAssistant.jsx'
// import PaymentSuccess from './PaymentSuccess.jsx'
// import PaymentFailure from './PaymentFailure.jsx'
// import ProtectedLayout from './components/ProtectedLayout.jsx'
// import './index.css'

// ReactDOM.createRoot(document.getElementById('root')).render(
//   <React.StrictMode>
//     <BrowserRouter>
//     <div className="relative min-h-screen">
//       <Routes>
//         <Route path="/" element={<App />} />
//         <Route path="/signup" element={<Signup />} />
//         <Route path="/dashboard" element={<Dashboard/>} />
//         <Route path='/forgot-password' element={<ForgotPassword/>} />
//         <Route path='/reset-password' element={<ResetPassword/>} />
//         <Route path='/travel-assistant' element={<TravelAssistant/>} />
//         <Route path='/Profile' element={<Profile/>}/>
//         <Route path='/my-trips' element={<MyTrips/>}/>
//         <Route path='/Back-to-dashboard' element={<BackToDashboardButton/>}/>
//         <Route path='/payment/:tripId' element={<PaymentPage/>}/>
//         <Route path='/payment/success' element={<PaymentSuccess/>}/>
//         <Route path='/payment/failure' element={<PaymentFailure/>}/>
//       </Routes>
//        {/*<AIAssistant/>*/}
//     </div>
//     </BrowserRouter>
//   </React.StrictMode>,
// )
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'

import App from './App.jsx'
import Signup from './Signup.jsx'
import Dashboard from './Dashboard.jsx'
import ForgotPassword from './Forgotpassword.jsx'
import ResetPassword from './ResetPassword.jsx'
import TravelAssistant from './TravelAssistant.jsx'
import Profile from './profile.jsx'
import MyTrips from './MyTrips.jsx'
import BackToDashboardButton from './components/BackToDashboardButton.jsx'
import PaymentPage from './PaymentPage.jsx'
import PaymentSuccess from './PaymentSuccess.jsx'
import PaymentFailure from './PaymentFailure.jsx'
import ProtectedLayout from './components/ProtectedLayout.jsx'   // ⭐ IMPORT LAYOUT HERE
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
        </Route>

      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
)