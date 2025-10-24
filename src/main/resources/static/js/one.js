// Recipe Buddy - Optimized
const RecipeBuddy = {
    init() {
        this.setupImagePreviews();
        this.setupStars();
        this.setupCheckboxes();
        this.setupDeletes();
    },

    setupImagePreviews() {
        const imageInputs = document.querySelectorAll('input[name="imageUrl"], input[name="profilePicture"]');
        imageInputs.forEach(input => {
            input.oninput = () => {
                const preview = document.getElementById('imagePreview');
                const url = input.value.trim();

                if (preview && url) {
                    preview.innerHTML = `<img src="${url}" class="img-fluid rounded" style="max-height: 200px;" onerror="this.parentElement.style.display='none'">`;
                    preview.style.display = 'block';
                } else if (preview) {
                    preview.style.display = 'none';
                }
            };
        });
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

    setupCheckboxes() {

        document.querySelectorAll('.ingredients-list .form-check-input').forEach(checkbox => {
            checkbox.onchange = function() {
                const item = this.closest('.ingredient-line');
                if (item) {
                    item.style.textDecoration = this.checked ? 'line-through' : 'none';
                    item.style.opacity = this.checked ? '0.7' : '1';
                }
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