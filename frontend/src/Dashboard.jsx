// // src/Dashboard.jsx
// export default function Dashboard() {
//   const token = localStorage.getItem("token");

//   if (!token) {
//     // If no token → redirect to login
//     window.location.href = "/";
//     return null;
//   }

//   return (
//     <div className="flex flex-col items-center justify-center h-screen bg-gray-100">
//       <h1 className="text-4xl font-bold mb-4">Welcome to SmartTrip 🎉</h1>
//       <p className="text-lg text-gray-700 mb-6">
//         You are successfully logged in. Your token is stored in localStorage.
//       </p>
//       <button
//         onClick={() => {
//           localStorage.removeItem("token"); // Logout
//           window.location.href = "/";
//         }}
//         className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition"
//       >
//         Logout
//       </button>
//     </div>
//   );
// }
import { useEffect, useState } from "react";

// Simple JWT decoder
function decodeToken(token) {
  try {
    const payload = token.split(".")[1];
    return JSON.parse(atob(payload));
  } catch (e) {
    return null;
  }
}

export default function Dashboard() {
  const token = localStorage.getItem("token");
  const [profile, setProfile] = useState(null);
  const [serverMessage, setServerMessage] = useState("");

  if (!token) {
    window.location.href = "/";
    return null;
  }

  useEffect(() => {
    // Decode JWT (for quick display)
    const decoded = decodeToken(token);
    const emailFromJwt = decoded?.sub;

    // Fetch profile from backend
    fetch("http://localhost:8081/api/auth/profile", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => {
        if (!res.ok) throw new Error("Unauthorized");
        return res.json();
      })
      .then((data) => setProfile(data))
      .catch(() => {
        localStorage.removeItem("token");
        window.location.href = "/";
      });

    // Fetch secure test route
    fetch("http://localhost:8081/api/test/secure", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    })
      .then((res) => res.ok ? res.text() : "Unauthorized")
      .then((msg) => setServerMessage(msg));
  }, [token]);

  return (
    <div className="flex flex-col items-center justify-center h-screen bg-gray-100 text-center">
      <h1 className="text-4xl font-bold mb-4">Welcome to SmartTrip 🎉</h1>

      {profile ? (
        <p className="text-lg text-gray-700">
          Logged in as: <span className="font-semibold">{profile.name}</span> 
          ({profile.email})
        </p>
      ) : (
        <p className="text-gray-500">Loading profile...</p>
      )}

      {serverMessage && (
        <p className="mt-4 text-green-600 font-medium">{serverMessage}</p>
      )}

      <button
        onClick={() => {
          localStorage.removeItem("token");
          window.location.href = "/";
        }}
        className="mt-6 bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition"
      >
        Logout
      </button>
    </div>
  );
}

