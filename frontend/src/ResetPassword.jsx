import { useState } from "react";
import { getPasswordStrength } from "./utils/passwordStrenth";

export default function ResetPassword() {
  const [formData, setFormData] = useState({
    password: "",
    confirmPassword: "",
  });
  const [strength, setStrength] = useState(null);
  const [loading, setLoading] = useState(false);

  // ✅ Extract token from URL (important)
  const token = new URLSearchParams(window.location.search).get("token");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });

    if (name === "password") {
      setStrength(getPasswordStrength(value));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      alert("Invalid or missing reset token. Please check your email link again.");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${import.meta.env.VITE_AUTH_SERVICE_URL}/api/auth/reset-password`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          token: token, // ✅ Send the token along with password
          newPassword: formData.password,
        }),
      });

      if (response.ok) {
        alert("✅ Password updated successfully!");
        window.location.href = "/"; // back to login
      } else {
        const err = await response.json();
        alert(`❌ Failed to reset password: ${err.error || "Unknown error"}`);
      }
    } catch (error) {
      console.error("Error:", error);
      alert("⚠️ Server error, please try again");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex h-screen justify-center items-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-lg w-96">
        <h2 className="text-2xl font-bold mb-6 text-gray-800">Reset Password</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Password */}
          <input
            type="password"
            name="password"
            placeholder="Enter new password"
            value={formData.password}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
          />

          {/* Strength Meter */}
          {strength && (
            <div>
              <div className="w-full bg-gray-200 h-2 rounded-lg">
                <div className={`h-2 rounded-lg ${strength.color}`}></div>
              </div>
              <p className="text-sm mt-1 text-gray-600">{strength.label}</p>
            </div>
          )}

          {/* Confirm Password */}
          <input
            type="password"
            name="confirmPassword"
            placeholder="Confirm new password"
            value={formData.confirmPassword}
            onChange={handleChange}
            className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
          />

          <button
            type="submit"
            disabled={loading}
            className={`mt-4 w-full p-3 rounded-lg transition ${
              loading
                ? "bg-gray-400 cursor-not-allowed"
                : "bg-blue-600 text-white hover:bg-blue-700"
            }`}
          >
            {loading ? "Updating..." : "Update Password"}
          </button>
        </form>
      </div>
    </div>
  );
}
