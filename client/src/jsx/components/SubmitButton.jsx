import React from "react";

export default function(isOn, callback) {
    return (
        <>
            <div className='login-email-submit' onClick={callback}
                style={{
                'backgroundColor':`${isOn ? 'blue':'rgb(226, 222, 222)'}`, 
                'color':`${isOn ? 'white': 'rgb(92, 92, 92)'}`}}>
                    Continue
            </div>
        </>
    )
}