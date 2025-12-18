import { useState } from 'react'

export default function (initialData, validationRules) {
  const [formData, setFormData] = useState(initialData)
  const [isError, setIsError] = useState(
    Object.keys(initialData).reduce((acc, key) => {
      acc[key] = false
      return acc
    }, {})
  )

  const setErrorTrue = (field) => {
    setIsError((prev) => ({ ...prev, [field]: true }))
  }

  const setErrorFalse = (field) => {
    setIsError((prev) => ({ ...prev, [field]: false }))
  }

  const updateField = (field, value) => {
    setFormData((prev) => ({ ...prev, [field]: value }))
  }

  const validateForm = () => {
    let hasError = false

    Object.entries(validationRules).forEach(([field, validator]) => {
      const value = formData[field]
      const isValid = validator(value)

      if (!isValid) {
        setErrorTrue(field)
        hasError = true
      } else {
        setErrorFalse(field)
      }
    })

    return hasError
  }

  return {
    formData,
    isError,
    updateField,
    validateForm
  }
}
