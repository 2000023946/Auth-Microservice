import { Form, redirect } from 'react-router-dom'
import '../css/signup.css'

import { useState, useEffect, useRef } from 'react'

import { useNavigate } from 'react-router-dom'
import InputField from './components/InputField'
import CheckBox from './components/CheckBox'
import useForm from './utils/useForm'
import SubmitButton from './components/SubmitButton'
import { sendRequest } from './utils/api'
import { validateEmail, validateInput, validatePassword } from './utils/validators'

export default function SignUp(props){

    const navigate = useNavigate()

    const validationRules = {
        email: validateEmail,
        password: validatePassword,
        passwordRepeat: validatePassword,
        terms: (val) => val === true,
    }

    const initialData = {
        email: '',
        password: '',
        passwordRepeat: '',
        terms: false,
    }

    const {
        formData,
        isError,
        updateField,
        validateForm
    } = useForm(initialData, validationRules)



    const submitFormData = (event) =>{
        console.log(formData)

        if (validateForm()) {
            return
        }

        console.log('created new user')

        const dataToSend = {
            'email' : formData.email,
            'pass1' : formData.password,
            'pass2' : formData.passwordRepeat
        }

        sendRequest('/api/auth/register', 'POST', dataToSend, (data) => {
            console.log('Success:', data)
            navigate('/login')
        })
        .catch(err => {
            console.log(err.message)
        })
    }



    const handleRedirect = (event) =>{
        const redirectLocation = event.target.dataset['redirect']
        navigate('/'+redirectLocation)
    }


    return (
        <div className="register">
            <div className='register-container'> 
                <div className='register-info-container'>
                    <div className='register-title'> Let's create your account.</div>
                    <div className='register-desc-contaienr'>
                        <div className='register-desc'> Signing up for Square is fast and free.</div>
                        <div className='register-desc'>No commitments or long-term contracts required.</div>
                    </div>
                </div> 
                <div className='register-form'>
                    <div className='register-auth-info'>
                        <InputField name={'email'} setFormData={updateField} placeholder='you@gmail.com' isError={isError.email} type={'text'}/>
                        <InputField name={'password'} setFormData={updateField} placeholder='very secure password' isError={isError.password} type={'password'}/>
                        <InputField name={'passwordRepeat'} setFormData={updateField} placeholder='very secure password' isError={isError.password} type={'password'}/>
                    </div>
                    <CheckBox terms={formData.terms} setFormData={updateField} isError={isError.terms}/>
                    <div className='register-submit' onClick={submitFormData}>Continue</div>
                </div>
                <div className='register-footer'>
                    <div className='register-footer-msg'>
                        Already have a Kar account? 
                        <span onClick={handleRedirect} data-redirect='login' className='register-sign-in-span'>Sign In</span>
                        </div>
                </div>
            </div> 
        </div>
    )
}
