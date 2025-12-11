
// Shopping List JavaScript
document.addEventListener('DOMContentLoaded', function() {

    // ========== Toggle Item Completion (Optional - for immediate visual feedback) ==========
    const toggleForms = document.querySelectorAll('form[action*="/toggle/"]');

    toggleForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Optional: Add visual feedback before form submits
            const item = form.closest('.shopping-item');
            if (item) {
                item.style.opacity = '0.5';
                item.style.transition = 'opacity 0.3s ease';
            }
        });
    });

    // ========== Confirm Delete Actions ==========
    const deleteForms = document.querySelectorAll('form[action*="/delete/"]');

    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });

    // ========== Bulk Actions Confirmation ==========
    const markAllForm = document.querySelector('form[action*="/mark-all-complete"]');
    if (markAllForm) {
        markAllForm.addEventListener('submit', function(e) {
            if (!confirm('Mark all items as complete?')) {
                e.preventDefault();
            }
        });
    }

    const removeCompletedForm = document.querySelector('form[action*="/remove-completed"]');
    if (removeCompletedForm) {
        removeCompletedForm.addEventListener('submit', function(e) {
            if (!confirm('Remove all completed items?')) {
                e.preventDefault();
            }
        });
    }

    const clearAllForm = document.querySelector('form[action*="/clear-all"]');
    if (clearAllForm) {
        clearAllForm.addEventListener('submit', function(e) {
            if (!confirm('Clear entire shopping list? This cannot be undone.')) {
                e.preventDefault();
            }
        });
    }

    // ========== Sidebar Toggle ==========
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    const dashboardContainer = document.querySelector('.dashboard-container');

    if (sidebarToggle && dashboardContainer) {
        sidebarToggle.addEventListener('click', function() {
            dashboardContainer.classList.toggle('sidebar-collapsed');
        });
    }

    // ========== Add Item Form Enhancement ==========
    const addItemForm = document.querySelector('form[action*="/add"]');
    if (addItemForm) {
        const nameInput = addItemForm.querySelector('input[name="name"]');

        // Autofocus on name input when page loads
        if (nameInput) {
            nameInput.focus();
        }

        // Clear form after successful submission (optional)
        // This is handled by redirect, but you can add animation here
    }

    // ========== Category Section Collapse (Optional Enhancement) ==========
    const categoryHeaders = document.querySelectorAll('.category-header');

    categoryHeaders.forEach(header => {
        header.style.cursor = 'pointer';

        header.addEventListener('click', function() {
            const itemsContainer = this.nextElementSibling;
            if (itemsContainer && itemsContainer.classList.contains('list-group')) {
                itemsContainer.style.display =
                    itemsContainer.style.display === 'none' ? 'block' : 'none';

                const icon = this.querySelector('i');
                if (icon) {
                    icon.classList.toggle('bi-tag-fill');
                    icon.classList.toggle('bi-tag');
                }
            }
        });
    });

    // ========== Smooth Scroll for Long Lists ==========
    const shoppingItems = document.querySelectorAll('.shopping-item');
    if (shoppingItems.length > 10) {
        // Add "Back to Top" button functionality if needed
        const backToTopBtn = document.createElement('button');
        backToTopBtn.innerHTML = '<i class="bi bi-arrow-up-circle-fill"></i>';
        backToTopBtn.className = 'btn btn-primary position-fixed bottom-0 end-0 m-4';
        backToTopBtn.style.display = 'none';
        backToTopBtn.style.zIndex = '1000';

        document.body.appendChild(backToTopBtn);

        window.addEventListener('scroll', function() {
            if (window.scrollY > 300) {
                backToTopBtn.style.display = 'block';
            } else {
                backToTopBtn.style.display = 'none';
            }
        });

        backToTopBtn.addEventListener('click', function() {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // ========== Recipe Dropdown Enhancement ==========
    const recipeSelect = document.querySelector('select[name="recipeId"]');
    if (recipeSelect) {
        recipeSelect.addEventListener('change', function() {
            if (this.value) {
                // Optional: Show preview or additional info
                console.log('Selected recipe:', this.value);
            }
        });
    }

    // ========== Animation for Newly Added Items (if you want) ==========
    // This would require session attribute or URL parameter
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('added') === 'true') {
        const lastItem = document.querySelector('.shopping-item:first-child');
        if (lastItem) {
            lastItem.style.animation = 'fadeInSlide 0.5s ease';
        }
    }
});

// ========== CSS Animation (add to CSS if you use the animation above) ==========
// @keyframes fadeInSlide {
//     from {
//         opacity: 0;
//         transform: translateX(-20px);
//     }
//     to {
//         opacity: 1;
//         transform: translateX(0);
//     }
// }