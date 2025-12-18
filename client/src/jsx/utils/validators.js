export function validateEmail(email) {
    console.log(email)
  if (!email) return false

  return email.includes('@') && email.includes('.')
}

export function validatePassword(password) {
  if (!password) return false

  return password.length >= 8
}

export function validateInput(input) {
    return true
}
