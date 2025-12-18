import '../css/login.css'
import { Link, useNavigate } from 'react-router-dom'

import { validateEmail, validatePassword } from './utils/validators'

import InputField from './components/InputField'

import useForm from './utils/useForm'
import { sendRequest } from './utils/api'


export default function LoginEmail(props){

    const navigate = useNavigate()

    const validationRules = {
        email: validateEmail,
        password: validatePassword,
        terms: (val) => val === true,
    }

    const initialData = {
        email: '',
        password: '',
    }

    const {
        formData,
        isError,
        updateField,
        validateForm
    } = useForm(initialData, validationRules)

    

    const handleRedirect = (event) =>{
        const redirectLocation = event.target.dataset['redirect']
        navigate('/'+redirectLocation)
    }

    const loginUser = (event) => {
        event.preventDefault();
        
        if (!validateForm()) return;


        sendRequest('/api/auth/login', 'POST', formData, (data) => {
            console.log('Success:', data)
            navigate('/welcome')
        })
        .catch(err => {
            console.log(err.message)
        })
    };


    return(
        <div className='login-email' >
            <div className='login-email-container'>
                <div className='login-email-title'>Sign in</div>
                <div className='login-email-sign-up'>New to Kar? <span className='login-email-redirect-sign-up' onClick={handleRedirect} data-redirect='sign-up'>Sign up</span></div>

                     <div className='register-auth-info'>
                        <InputField name={'email'} setFormData={updateField} placeholder='you@gmail.com' isError={isError.email} type={'text'}/>
                        <InputField name={'password'} setFormData={updateField} placeholder='very secure password' isError={isError.password} type={'password'}/>
                    </div>
                <div className="login-password-forgot-password" onClick={handleRedirect} data-redirect='forgot'>Forgot password?</div>

                <div className='register-submit' onClick={loginUser}>
                        Continue
                </div>
                
            </div>
        </div>
    )
}

