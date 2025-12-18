import SignUp from './jsx/signup'
import Login from './jsx/login'
import LoginPassword from './jsx/loginPassword';
import ForgotPassword from './jsx/forgotPassword'
import ResetPassword from './jsx/resetPassword'
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState } from 'react';
import Welcome from './jsx/welcome';

function App() {

  const [data, setData] = useState({
    'dataprojects':[],
    'name':'',
    'jwt':'',
    'dataproject':null
  })

  const props = {
    ...data,
    'setter':setData
  }

  return (
    <Router>
      <Routes>
        <Route path='/forgot' element={<ForgotPassword/>}/>
        <Route path="/reset/:uidb64/:token/" element={<ResetPassword />} />
        <Route path='/login' element={<Login {...props}/>}/>
        <Route path='/sign-up' element={<SignUp/>}/>
        <Route path='/welcome' element={<Welcome />} />
      </Routes>
    </Router>
  );
}

export default App;
