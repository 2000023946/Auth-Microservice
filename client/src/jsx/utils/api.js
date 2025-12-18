export async function sendRequest(path, method = 'GET', data = null, onSuccess) {
  try {
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
      },
    }

    if (data) {
      options.body = JSON.stringify(data)
    }

    console.log(data, 'data to submit')

    const response = await fetch(path, options)

    console.log('response was submited')

    // Check for HTTP errors
    if (!response.ok) {
      const errorText = await response.text(); // Use .text() instead of .json() to see the error
      console.log('Error Body:', errorText);
      throw new Error(`Request failed: ${response.status}`);
    }

    const responseData = await response.json()

    console.log('response was good', responseData)
    
    if (onSuccess) onSuccess(responseData)
    
    return responseData
  } catch (error) {
    console.error('Error:', error)
    throw error // let caller handle it if needed
  }
}
