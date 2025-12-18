import { useState } from "react"

export default function InputField({name, setFormData, inputPlaceholder, isError, type}) {

	const [placeholder, setPlaceholder] = useState(inputPlaceholder)

	const handleAnimation = (event) =>{
		console.log(event.target.nextElementSibling)
		const sibling = event.target.nextElementSibling
		sibling.style.color = 'black'
		sibling.style.fontSize = '13px'
		sibling.style.fontWeight = '600'
		sibling.style.transform = 'translateY(-15px)'
		setPlaceholder('')
	}


	const updateFormData = (event) =>{
		console.log(event.target.value, name, event.target)
		const value = name === 'terms' ? !formData.terms : event.target.value
		setFormData(name, value)
	}

  return (
    <>
			<div className='register-username-container'>
					<input type={type} onChange={updateFormData} data-name='email' onClick={handleAnimation} id='email' placeholder={placeholder}/>
					<label for='email' id='label-email'>{name}</label>
			</div>
			{isError && <div className='register-error'> Please Enter a valid {name}</div>}
    </>
  )
}
