import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AIAssistant from "./components/AIAssistant";

// Simple JWT decoder
function decodeToken(token) {
  try {
    const payload = token.split(".")[1];
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}

export default function Dashboard() {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");
  const [profile, setProfile] = useState(null);
  const [showAssistant, setShowAssistant] = useState(true); // ✅ Default true to auto-open AI

  if (!token) {
    window.location.href = "/";
    return null;
  }

   useEffect(() => {
  fetch("http://localhost:8081/api/auth/profile", {
    headers: { Authorization: `Bearer ${token}` },
  })
    .then((res) => {
      if (!res.ok) throw new Error("Unauthorized");
      return res.json();
    })
    .then((data) => {
      setProfile(data);

      // 🧠 If payment was just completed, trigger AI greeting
      if (localStorage.getItem("paymentSuccess") === "true") {
        setTimeout(() => {
          setShowAssistant(true);
          localStorage.removeItem("paymentSuccess"); // clear flag
        }, 1000);
      }
    })
    .catch(() => {
      localStorage.removeItem("token");
      window.location.href = "/";
    });
}, [token]);

  return (
    <div className="relative min-h-screen bg-gradient-to-br from-blue-100 to-blue-200 text-center flex flex-col items-center justify-center">
      <h1 className="text-4xl font-bold mb-6 text-blue-800">
        Welcome to SmartTrip 🌍
      </h1>

      {profile ? (
        <p className="text-lg text-gray-800 mb-8">
          Logged in as{" "}
          <span className="font-semibold">{profile.name}</span> ({profile.email})
        </p>
      ) : (
        <p className="text-gray-600">Loading profile...</p>
      )}

      {/* Navigation Buttons */}
      <div className="grid grid-cols-2 gap-6">
        <button
          onClick={() => navigate("/travel-assistant")}
          className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-xl shadow-lg transition"
        >
          🧭 Trip Planner
        </button>

        <button
          onClick={() => navigate("/my-trips")}
          className="bg-green-600 hover:bg-green-700 text-white px-6 py-3 rounded-xl shadow-lg transition"
        >
          🌍 My Trips
        </button>

        <button
          onClick={() => navigate("/profile")}
          className="bg-purple-600 hover:bg-purple-700 text-white px-6 py-3 rounded-xl shadow-lg transition"
        >
          👤 Profile
        </button>

        {/* <button
          onClick={() => {
            localStorage.removeItem("token");
            navigate("/");
          }}
          className="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-xl shadow-lg transition"
        >
          🔓 Logout
        </button> */}
        <button
        onClick={() => {
          localStorage.removeItem("token");
          localStorage.removeItem("chatHistory");  // ⭐ clear chat
          localStorage.removeItem("paymentSuccess"); // ⭐ clear payment flag
          navigate("/");  // redirect to homepage/login
        }}
        className="bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-xl shadow-lg transition"
      >
        🔓 Logout
      </button>
      </div>

      {/* ✅ Auto-popup AI Assistant */}
      {showAssistant && (
        <AIAssistant
          userName={profile?.name || "Traveler"}
          onClose={() => setShowAssistant(false)}
        />
      )}
    </div>
  );
}
