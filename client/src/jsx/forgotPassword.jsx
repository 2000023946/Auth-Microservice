import { redirect } from 'react-router-dom'
import '../css/login.css'
import { Link, useNavigate } from 'react-router-dom'

import {useRef, useState} from 'react'


export default function LoginEmail(props){

    const navigate = useNavigate()

    const emailLabelRef = useRef(null)
    const emailInputRef = useRef(null)

    const [emailData, setEmailData] = useState('')

    const [errorMessage, setErrorMessage] = useState('')

    const handleStyleChange = (event) => {
        function applyFilters(ref) {
            ref.current.style.transform = 'translateY(-100%)'
            ref.current.style.fontWeight = 600
            ref.current.style.fontSize = '15px'
            ref.current.style.color = 'black'
            ref.current.style.padding = '30px 10px 10px 10px'
        }
    
        const ref = emailLabelRef
        applyFilters(ref)
    }
    

    const handleToggle = (event) =>{
        function applyFilters(ref){
            ref.current.style.transform = 'translateY(-50%)'
            ref.current.style.color = 'gray'
            ref.current.style.fontSize = '20px'
            ref.current.style.padding = '20px 10px 20px 10px'
        }
        if (event.target.dataset['name'] !== 'input' && emailData === ''){
            applyFilters(emailLabelRef)
        }
    }
    const updateEmail = (event) =>{
        setEmailData(event.target.value)
    }
    const isEmailValid = () =>{
        return emailData.includes('@') && emailData.includes('.') && emailData.trim().length > 5
    }


    const handleRedirect = (event) =>{
        const redirectLocation = event.target.dataset['redirect']
        navigate('/'+redirectLocation)
    }

    const loginUser = (event) =>{
        if (!isEmailValid()) return
        console.log(emailData)
        fetch('http://127.0.0.1:8000/api/user/forgot/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: emailData,
            })
        })
        .then(res => res.json())
        .then(data => {
            // Save JWT to localStorage
            console.log(data)
            setErrorMessage(data['message'])
        })
        .catch(err => console.error('Login failed', err));
    }



    return(
        <div className='login-email' onClick={handleToggle}>
            <div className='login-email-container'>
                <div className='login-email-title'>Forgot Password</div>
                <div className='login-email-sign-up'>New to Kar? <span className='login-email-redirect-sign-up' onClick={handleRedirect} data-redirect='sign-up'>Sign up</span></div>
                {errorMessage}
                <div className='login-email-email-container'>
                    <input id='login-email-input' name='email' ref={emailInputRef} onChange={updateEmail} data-name='input' onClick={handleStyleChange} type='email'/>
                    <label className='login-email-label' data-name='input' ref={emailLabelRef} htmlFor='login-email-input'>Email</label>
                </div>
                <div className="login-password-forgot-password" onClick={handleRedirect} data-redirect='login'>Login?</div>
                <div className='login-email-submit' onClick={loginUser} data-redirect='loginPassword'
                    style={{
                    'backgroundColor':`${isEmailValid()? 'blue':'rgb(226, 222, 222)'}`, 
                    'color':`${isEmailValid()? 'white': 'rgb(92, 92, 92)'}`}}>
                        Continue
                </div>
            </div>
        </div>
    )
}