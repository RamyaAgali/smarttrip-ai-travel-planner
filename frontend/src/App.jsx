import { useState } from "react";
import { Link } from "react-router-dom";

export default function Login() {
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8081/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("❌ Invalid email or password");
        }
        throw new Error("⚠️ Login failed, please try again");
      }

      // const data = await response.json();
      // localStorage.setItem("token", data.token); // ✅ Save JWT
      // alert("✅ Login successful!");
      // window.location.href = "/dashboard"; // redirect
       const data = await response.json();

        // ✅ Store token
        localStorage.setItem("token", data.token);

        // ✅ Check if backend sent user info
        if (data.user) {
          localStorage.setItem("userEmail", data.user.email);
          localStorage.setItem("userName", data.user.name);

          localStorage.setItem("user_profile", JSON.stringify({name : data.user.name, email: data.user.email}));
        } else {
          console.warn("⚠ No user object found in login response:", data);
        }

        // Optional: Log to verify
        console.log("🔹 Stored email:", localStorage.getItem("userEmail"));

        alert("✅ Login successful!");
        window.location.href = "/dashboard"; // redirect
    } catch (err) {
      setError(err.message || "🚨 Server error, please try again later");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex h-screen">
      {/* Left Section - Branding */}
      <div className="w-1/2 bg-blue-600 text-white flex flex-col justify-center items-center p-10">
        <h1 className="text-4xl font-bold mb-4">SmartTrip</h1>
        <p className="text-lg mb-6">Your AI-powered travel planner</p>
        <ul className="space-y-2 text-lg">
          <li>✨ Discover destinations with AI</li>
          <li>🗺️ Visualize trips on interactive maps</li>
          <li>🤖 Chat with your travel assistant</li>
        </ul>
      </div>

      {/* Right Section - Login Form */}
      <div className="w-1/2 flex justify-center items-center bg-white">
        <div className="w-full max-w-md p-8">
          <h2 className="text-2xl font-bold mb-6 text-gray-800 text-center">
            Login to SmartTrip
          </h2>
          <form className="space-y-4" onSubmit={handleSubmit}>
            <div>
              <label className="block text-gray-600">Email</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="you@example.com"
                required
                className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-gray-600">Password</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="••••••••"
                required
                className="w-full p-3 mt-1 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* Error Message */}
            {error && <p className="text-red-500 text-center">{error}</p>}

            <button
              type="submit"
              disabled={loading}
              className={`w-full p-3 rounded-lg transition ${
                loading
                  ? "bg-gray-400 cursor-not-allowed"
                  : "bg-blue-600 text-white hover:bg-blue-700"
              }`}
            >
              {loading ? "Logging in..." : "Login"}
            </button>

            <p className="text-center text-gray-500 mt-4">
              Don’t have an account?{" "}
              <a href="/signup" className="text-blue-600">
                Sign Up
              </a>
            </p>
            <p className="text-center text-gray-500 mt-4">
              <a href="/forgot-password" className="text-blue-600">Forgot Password?</a>
            </p>

          </form>
        </div>
      </div>
    </div>
  );
}
