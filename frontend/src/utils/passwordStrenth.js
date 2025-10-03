export function getPasswordStrength(password) {
  let score = 0;

  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;

  if (score <= 1) return { label: "Weak", color: "w-1/4 bg-red-500" };
  if (score === 2) return { label: "Medium", color: "w-2/4 bg-yellow-500" };
  if (score === 3) return { label: "Strong", color: "w-3/4 bg-green-500" };
  if (score === 4) return { label: "Very Strong", color: "w-full bg-blue-600" };
}
