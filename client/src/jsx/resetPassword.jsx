import {useRef, useState} from 'react'
import '../css/loginPassword.css'
import { useNavigate, useParams } from 'react-router-dom';

export default function LoginPassword(){
    const {uidb64, token} = useParams()

    const navigate = useNavigate()

    const passwordInputRef = useRef(null)
    const passwordLabelRef = useRef(null)

    const [passwordData, setPasswordData] = useState('')
    const [errorMessage, setErrorMessage] = useState('')

    const [imageUrl, setImageUrl] = useState('nosee.png')
    const [imageType, setImageType] = useState('password')


    const handleStyleChange = (event) =>{
        passwordLabelRef.current.style.transform = 'translateY(-150%)'
        passwordLabelRef.current.style.fontWeight = 600
        passwordLabelRef.current.style.fontSize = '15px'
        passwordLabelRef.current.style.color = 'black'
        passwordInputRef.current.style.padding = '30px 10px 10px 10px'
    }
    const handleToggle = (event) =>{
        if (event.target.dataset['name'] !== 'input' && passwordData === ''){
            passwordLabelRef.current.style.transform = 'translateY(-50%)'
            passwordLabelRef.current.style.color = 'gray'
            passwordLabelRef.current.style.fontSize = '20px'
            passwordInputRef.current.style.padding = '20px 10px 20px 10px'
        }
    }


    const handleRedirect = (event) =>{
        const location = event.target.dataset['redirect']
        navigate(location)
    }
    
    const updateData = (event) =>{
        setPasswordData(event.target.value)
    }

    const isPasswordValid =()=>{
        return passwordData.trim().length > 2
    }

    const changeImage = (event) =>{
        if(imageUrl === 'nosee.png'){
            setImageUrl('see.png')
            setImageType('text')

        }else{
            setImageUrl('nosee.png')
            setImageType('password')
        }
    }

    const validateUser = (event) =>{
        fetch(`http://localhost:8000/api/user/reset/${uidb64}/${token}/`, {
            method:'POST',
            headers: {
                'Content-Type':'application/json'
            },
            body: JSON.stringify({'password':passwordData})
        }).then((resp) => resp.json())
        .then(data =>{
            if (data['error']){
                setErrorMessage(data['error'])
            }else{
                navigate('/login')
            }
        })
    }

    return(
        <div className="login-email" onClick={handleToggle}>
            <div className="login-email-container">
                <div className='login-email-title'>Reset Password</div>
                {errorMessage}
                <div className='login-email-email-container'>
                    <input type={imageType} id='login-email-input' ref={passwordInputRef} onChange={updateData} data-name='input' onClick={handleStyleChange}/>
                    <label class='login-email-label' data-name='input' ref={passwordLabelRef} for='login-email-input'>Password</label>
                    <img onClick={changeImage}  className='login-password-image' src={`/${imageUrl}`} />
                </div>
                <div className='login-password-button-container'>
                    <div className='login-password-sign-in' onClick={validateUser} data-redirect='welcome'
                        style={{
                            'backgroundColor':`${isPasswordValid()? 'blue':'rgb(226, 222, 222)'}`, 
                            'color':`${isPasswordValid()? 'white': 'rgb(92, 92, 92)'}`}}>
                            Reset
                    </div>
                </div>
            </div>
        </div>
    )
}