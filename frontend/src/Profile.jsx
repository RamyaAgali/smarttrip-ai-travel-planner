import { useEffect, useState } from "react";
import BackToDashboardButton from "./components/BackToDashboardButton";

export default function Profile() {
  const token = localStorage.getItem("token");
  const [profile, setProfile] = useState(null);

  useEffect(() => {
    fetch(`${import.meta.env.VITE_AUTH_SERVICE_URL}/api/auth/profile`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Unauthorized");
        return res.json();
      })
      .then(setProfile)
      .catch(() => (window.location.href = "/"));
  }, [token]);

  return (
    <div className="relative flex flex-col items-center justify-center min-h-screen bg-gray-50">
      {/* 🔙 Back to Dashboard button (fixed position) */}
      <div className="absolute top-6 left-6">
        <BackToDashboardButton />
      </div>

      {profile ? (
        <div className="bg-white p-8 rounded-2xl shadow-md w-96 text-center border border-gray-200">
          <h1 className="text-2xl font-bold text-blue-700 mb-4">👤 My Profile</h1>
          <p className="text-gray-700">
            <strong>Name:</strong> {profile.name}
          </p>
          <p className="text-gray-700">
            <strong>Email:</strong> {profile.email}
          </p>
        </div>
      ) : (
        <p className="text-gray-600 mt-8">Loading profile...</p>
      )}
    </div>
  );
}
