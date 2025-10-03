import { useState } from "react";
import { getPasswordStrength } from "./utils/passwordStrenth";

export default function ResetPassword() {
  const [formData, setFormData] = useState({
    password: "",
    confirmPassword: "",
  });
  const [strength, setStrength] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });

    if (name === "password") {
      setStrength(getPasswordStrength(value));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (formData.password !== formData.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    // 👉 Here call your backend API to update password
    try {
      const response = await fetch("http://localhost:8081/api/auth/reset-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          password: formData.password,
        }),
      });

      if (response.ok) {
        alert("Password updated successfully!");
        window.location.href = "/"; // back to login
      } else {
        alert("Failed to reset password");
      }
    } catch (error) {
      console.error("Error:", error);
      alert("Server error, please try again");
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
            className="mt-4 w-full bg-blue-600 text-white p-3 rounded-lg hover:bg-blue-700 transition"
          >
            Update Password
          </button>
        </form>
      </div>
    </div>
  );
}
