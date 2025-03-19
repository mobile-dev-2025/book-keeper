const express = require('express');
const router = express.Router();

// Check user existence and create if needed 

router.post('/checkUser', async (req, res) => {
   try {
   
    // Extract Auth0 subId from verified token
      const subId = req.user?.subId; 
     
      console.log(subId);
      if (!subId) {
        return res.status(401).json({ error: 'Missing authentication credentials' });
        
      }
    //Check existing user using Auth0 sub
     const user = await User.findOne({ auth0Id: subId });
     let isNewUser = false;
  
     //Create user if not found
    if (!user) {
      user = await User.create({
        auth0Id: subId,
        createdAt: new Date(),
        lastLogin: new Date()
      });
      
      isNewUser = true;
    } else {
      // Update last login for existing users
      user.lastLogin = new Date();
      await user.save();
    }

      res.json({
      isNewUser,
      auth0Id: user.auth0Id 
    });
    
  } catch (error) {
    console.error('User check error:', error);
      res.status(500).json({ 
        error: 'User processing failed',
        details: error.message
      });
  }
});
  
module.exports = router;

