import { useState, useRef } from "react"

export default function({terms, setFormData, isError}) {

    const label = useRef(null)

    const handleCheck = (event) =>{
        console.log(terms)
        if(terms){
            console.log('turn white')
            label.current.style.setProperty('--background-color', 'white')
            label.current.style.setProperty('--border-color', 'rgb(206, 206, 206)')
        }else{
            console.log('turn blue')
            label.current.style.setProperty('--background-color', 'blue')
            label.current.style.setProperty('--border-color', 'blue')
        }
        setFormData('terms', !terms)
    }

    return (
        <>
            <div className='register-form-checkbox' onClick={handleCheck} data-name='terms'>
                <input type='checkbox' data-name='terms' id='countries' />
                <label for='countries' ref={label} onClick={handleCheck} data-name='terms'>I agree to Square's Terms, Privacy Policy, and E-Sign Consent.</label>
            </div>
            {isError && <div className='register-error'> Please Agree to the Terms and Conditions</div>}
        </>
    )
}