const express = require('express');
const router = express.Router();


// Check user existence and create if needed still incomplete though
router.post('/checkUser', async (req, res) => {
  const { email, password } = req.body;
  try {
    let user = await User.findOne({ email });
    if (!user) {
      user = new User({ email, password });
      await user.save();
    }
    const token = jwt.sign({ userId: user._id }, process.env.JWT_SECRET);
    res.send({ user, token });
  } catch (error) {
    res.status(400).send(error);
  }
});

module.exports = router;