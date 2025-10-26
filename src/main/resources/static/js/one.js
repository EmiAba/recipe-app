
const RecipeBuddy = {
    init() {
       this.setupStars();
        this.setupDeletes();
    },


    setupStars() {
        const stars = document.querySelectorAll('.star');
        if (!stars.length) return;

        const ratingInput = document.getElementById('rating-input');
        const ratingText = document.getElementById('rating-text');
        const labels = ['', 'Poor', 'Fair', 'Good', 'Very Good', 'Excellent'];

        stars.forEach((star, index) => {
            star.onclick = () => {
                const rating = index + 1;
                if (ratingInput) ratingInput.value = rating;
                if (ratingText) ratingText.textContent = labels[rating];

                stars.forEach((s, i) => {
                    s.style.opacity = i < rating ? '1' : '0.3';
                });
            };
        });
    },



    setupDeletes() {

        document.querySelectorAll('form[action*="/delete"]').forEach(form => {
            form.onsubmit = () => confirm('Are you sure you want to delete this?');
        });


        document.querySelectorAll('button[onclick*="confirm"]').forEach(btn => {
            btn.onclick = (e) => {
                if (!confirm('Are you sure?')) e.preventDefault();
            };
        });
    }
};


document.addEventListener('DOMContentLoaded', () => RecipeBuddy.init());